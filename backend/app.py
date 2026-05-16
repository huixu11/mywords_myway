import os
import shutil
import uuid
from datetime import datetime, timedelta, timezone
from pathlib import Path

from fastapi import FastAPI, File, Form, HTTPException, Query, UploadFile
from fastapi.responses import FileResponse
from fastapi.staticfiles import StaticFiles
import uvicorn

from db import connect, init_db, rows_to_dicts
from gemma_client import extract_nouns, safety_check, transcribe_audio_mock

ROOT = Path(__file__).resolve().parents[1]
STATIC_DIR = ROOT / "static"
DATA_DIR = Path(os.getenv("APP_DATA_DIR", ROOT / "data"))
AUDIO_DIR = Path(os.getenv("AUDIO_DIR", DATA_DIR / "audio"))
STORAGE_LIMIT_BYTES = int(os.getenv("APP_STORAGE_LIMIT_BYTES", str(1024 * 1024 * 1024)))

app = FastAPI(title="My Words, My Way")
app.mount("/static", StaticFiles(directory=STATIC_DIR), name="static")

def now():
    return datetime.now(timezone.utc).isoformat()

def _current_week_start() -> str:
    current = datetime.now(timezone.utc)
    start = current - timedelta(days=current.weekday())
    return start.replace(hour=0, minute=0, second=0, microsecond=0).isoformat()

def _data_size_bytes() -> int:
    if not DATA_DIR.exists():
        return 0
    total = 0
    for path in DATA_DIR.rglob("*"):
        if path.is_file():
            try:
                total += path.stat().st_size
            except OSError:
                pass
    return total

def _date_bound(value: str | None, *, end: bool = False) -> str | None:
    if not value:
        return None
    try:
        if "T" not in value and len(value) == 10:
            dt = datetime.fromisoformat(value).replace(tzinfo=timezone.utc)
            if end:
                dt += timedelta(days=1)
        else:
            dt = datetime.fromisoformat(value.replace("Z", "+00:00"))
            if dt.tzinfo is None:
                dt = dt.replace(tzinfo=timezone.utc)
    except ValueError as exc:
        raise HTTPException(status_code=400, detail="Dates must be YYYY-MM-DD or ISO timestamps.") from exc
    return dt.isoformat()

def _audio_path_in_storage(audio_path: str) -> Path:
    path = Path(audio_path).resolve()
    audio_root = AUDIO_DIR.resolve()
    try:
        path.relative_to(audio_root)
    except ValueError as exc:
        raise HTTPException(status_code=400, detail="Stored audio path is outside the audio directory.") from exc
    return path

def _cleanup_storage_if_needed() -> dict:
    deleted_audio = 0
    deleted_transcripts = 0
    if _data_size_bytes() <= STORAGE_LIMIT_BYTES:
        return {
            "storage_limit_bytes": STORAGE_LIMIT_BYTES,
            "storage_size_bytes": _data_size_bytes(),
            "deleted_audio": deleted_audio,
            "deleted_transcripts": deleted_transcripts,
        }

    timestamp = now()
    with connect() as conn:
        memos = conn.execute(
            """SELECT id, audio_path, transcript
               FROM voice_memos
               WHERE audio_path != '' OR COALESCE(transcript, '') != ''
               ORDER BY created_at ASC"""
        ).fetchall()
        for memo in memos:
            if _data_size_bytes() <= STORAGE_LIMIT_BYTES:
                break
            if memo["audio_path"]:
                audio_path = _audio_path_in_storage(memo["audio_path"])
                if audio_path.exists():
                    audio_path.unlink()
                    deleted_audio += 1
                conn.execute(
                    "UPDATE voice_memos SET audio_path='', audio_deleted_at=? WHERE id=?",
                    (timestamp, memo["id"])
                )
            if memo["transcript"]:
                conn.execute(
                    "UPDATE voice_memos SET transcript='', transcript_deleted_at=? WHERE id=?",
                    (timestamp, memo["id"])
                )
                deleted_transcripts += 1

    return {
        "storage_limit_bytes": STORAGE_LIMIT_BYTES,
        "storage_size_bytes": _data_size_bytes(),
        "deleted_audio": deleted_audio,
        "deleted_transcripts": deleted_transcripts,
    }

def _query_note_rows(q: str = "", start_date: str | None = None, end_date: str | None = None, min_memos: int = 1):
    start_bound = _date_bound(start_date)
    end_bound = _date_bound(end_date, end=True)
    clauses = ["TRIM(COALESCE(c.user_note, '')) != ''"]
    params: list[str | int] = []

    if q:
        clauses.append("(c.user_note LIKE ? OR c.title LIKE ?)")
        like = f"%{q}%"
        params.extend([like, like])
    if start_bound:
        clauses.append("c.created_at >= ?")
        params.append(start_bound)
    if end_bound:
        clauses.append("c.created_at < ?")
        params.append(end_bound)

    where = " AND ".join(clauses)
    sql = f"""
        SELECT c.id, c.created_at, c.title, c.user_note, COUNT(vm.id) AS memo_count
        FROM conversations c
        LEFT JOIN voice_memos vm ON vm.conversation_id = c.id
        WHERE {where}
        GROUP BY c.id
        HAVING COUNT(vm.id) >= ?
        ORDER BY c.created_at DESC
    """
    params.append(min_memos)
    with connect() as conn:
        return conn.execute(sql, params).fetchall()

@app.on_event("startup")
def startup():
    init_db()
    AUDIO_DIR.mkdir(parents=True, exist_ok=True)

@app.get("/")
def index():
    return FileResponse(STATIC_DIR / "index.html")

@app.post("/api/conversations")
def create_conversation():
    cid = str(uuid.uuid4())
    with connect() as conn:
        conn.execute(
            """INSERT INTO conversations
               (id, created_at, title, user_note, payment_status, is_free_weekly)
               VALUES (?, ?, ?, ?, ?, ?)""",
            (cid, now(), "Untitled reflection", "", "free", 1)
        )
        conn.commit()
    return {
        "conversation_id": cid,
        "question": "What does that make you think about?",
        "payment_status": "free",
        "free_conversation_available": True,
    }

@app.post("/api/conversations/{conversation_id}/memos")
async def add_memo(conversation_id: str, audio: UploadFile | None = File(default=None), text: str = Form(default="")):
    memo_id = str(uuid.uuid4())
    audio_path = ""

    if audio is not None:
        suffix = Path(audio.filename or "memo.webm").suffix or ".webm"
        audio_path = str(AUDIO_DIR / f"{memo_id}{suffix}")
        with open(audio_path, "wb") as f:
            shutil.copyfileobj(audio.file, f)
        transcript = transcribe_audio_mock(audio.filename or "memo.webm")
    else:
        transcript = text.strip() or transcribe_audio_mock("demo")

    risk = safety_check(transcript)

    with connect() as conn:
        conn.execute(
            "INSERT INTO voice_memos (id, conversation_id, audio_path, transcript, created_at, visible_to_user) VALUES (?, ?, ?, ?, ?, 0)",
            (memo_id, conversation_id, audio_path, transcript, now())
        )
        conn.execute(
            "UPDATE conversations SET safety_status=? WHERE id=?",
            (risk["risk_level"], conversation_id)
        )
        conn.commit()
    storage_cleanup = _cleanup_storage_if_needed()
    with connect() as conn:
        stored_memo = conn.execute(
            "SELECT audio_path, transcript FROM voice_memos WHERE id=?",
            (memo_id,)
        ).fetchone()

    return {
        "memo_id": memo_id,
        "audio_saved": bool(stored_memo["audio_path"]) if stored_memo else False,
        "transcript_saved_hidden": bool(stored_memo["transcript"]) if stored_memo else False,
        "storage_cleanup": storage_cleanup,
        "next_question": None if risk["should_stop_reflection_loop"] else "What does that make you think about?",
        "safety": risk
    }

@app.get("/api/conversations/{conversation_id}/memos")
def list_conversation_memos(conversation_id: str):
    with connect() as conn:
        rows = conn.execute(
            """SELECT id, created_at, audio_path, audio_deleted_at, transcript, transcript_deleted_at
               FROM voice_memos
               WHERE conversation_id=?
               ORDER BY created_at ASC""",
            (conversation_id,)
        ).fetchall()
    return {
        "memos": [
            {
                "id": row["id"],
                "created_at": row["created_at"],
                "has_audio": bool(row["audio_path"]),
                "audio_deleted_at": row["audio_deleted_at"],
                "transcript_deleted_at": row["transcript_deleted_at"],
                "transcript_saved_hidden": bool(row["transcript"]),
            }
            for row in rows
        ]
    }

@app.delete("/api/memos/{memo_id}/audio")
def delete_memo_audio(memo_id: str):
    with connect() as conn:
        memo = conn.execute(
            "SELECT id, audio_path, transcript FROM voice_memos WHERE id=?",
            (memo_id,)
        ).fetchone()
        if not memo:
            raise HTTPException(status_code=404, detail="Memo not found.")

        file_deleted = False
        if memo["audio_path"]:
            audio_path = _audio_path_in_storage(memo["audio_path"])
            if audio_path.exists():
                audio_path.unlink()
                file_deleted = True

        timestamp = now()
        conn.execute(
            "UPDATE voice_memos SET audio_path='', audio_deleted_at=? WHERE id=?",
            (timestamp, memo_id)
        )
        conn.commit()

    return {
        "status": "audio_deleted",
        "memo_id": memo_id,
        "file_deleted": file_deleted,
        "transcript_saved_hidden": bool(memo["transcript"]),
    }

@app.post("/api/conversations/{conversation_id}/finish")
def finish_conversation(conversation_id: str, user_note: str = Form(...)):
    clean_note = user_note.strip()
    if not clean_note:
        raise HTTPException(
            status_code=400,
            detail="Write your own note before asking Gemma for noun suggestions."
        )

    with connect() as conn:
        conn.execute(
            "UPDATE conversations SET user_note=?, title=? WHERE id=?",
            (clean_note, clean_note[:60], conversation_id)
        )
        transcripts = conn.execute(
            "SELECT transcript FROM voice_memos WHERE conversation_id=? AND COALESCE(transcript, '') != '' ORDER BY created_at ASC",
            (conversation_id,)
        ).fetchall()
        existing = conn.execute("SELECT noun FROM nouns WHERE status='confirmed'").fetchall()

    full_transcript = "\n".join([row["transcript"] for row in transcripts])
    result = extract_nouns(full_transcript, [row["noun"] for row in existing])

    with connect() as conn:
        for item in result.get("candidate_nouns", []):
            conn.execute(
                """INSERT INTO noun_suggestions
                   (id, conversation_id, suggested_noun, suggestion_type, visible_reason, evidence_span, scene_type, confidence, status, created_at)
                   VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'pending', ?)""",
                (
                    str(uuid.uuid4()), conversation_id, item["noun"], item.get("suggestion", "add"),
                    item.get("visible_reason", ""), item.get("evidence_span", ""),
                    item.get("scene_type", "unclear"), item.get("confidence", "low"), now()
                )
            )
        conn.commit()

    return {"candidate_nouns": result.get("candidate_nouns", [])}

@app.get("/api/suggestions")
def list_suggestions():
    with connect() as conn:
        rows = conn.execute("SELECT * FROM noun_suggestions ORDER BY created_at DESC").fetchall()
    return {"suggestions": rows_to_dicts(rows)}

@app.post("/api/suggestions/{suggestion_id}/accept")
def accept_suggestion(suggestion_id: str):
    with connect() as conn:
        s = conn.execute("SELECT * FROM noun_suggestions WHERE id=?", (suggestion_id,)).fetchone()
        if not s:
            return {"error": "not_found"}
        noun_id = str(uuid.uuid4())
        timestamp = now()
        conn.execute(
            "INSERT INTO nouns (id, noun, status, created_at, updated_at) VALUES (?, ?, 'confirmed', ?, ?)",
            (noun_id, s["suggested_noun"], timestamp, timestamp)
        )
        note = conn.execute("SELECT user_note FROM conversations WHERE id=?", (s["conversation_id"],)).fetchone()
        conn.execute(
            """INSERT INTO noun_links
               (id, noun_id, conversation_id, transcript_span, visible_note_excerpt, scene_type, created_at)
               VALUES (?, ?, ?, ?, ?, ?, ?)""",
            (
                str(uuid.uuid4()), noun_id, s["conversation_id"], s["evidence_span"],
                note["user_note"] if note else "", s["scene_type"], timestamp
            )
        )
        conn.execute("UPDATE noun_suggestions SET status='accepted' WHERE id=?", (suggestion_id,))
        conn.commit()
    return {"status": "accepted", "noun_id": noun_id, "linked_note": note["user_note"] if note else ""}

@app.post("/api/suggestions/{suggestion_id}/reject")
def reject_suggestion(suggestion_id: str):
    with connect() as conn:
        conn.execute("UPDATE noun_suggestions SET status='rejected' WHERE id=?", (suggestion_id,))
        conn.commit()
    return {"status": "rejected"}

@app.get("/api/nouns")
def list_nouns():
    with connect() as conn:
        rows = conn.execute("SELECT * FROM nouns WHERE status='confirmed' ORDER BY updated_at DESC").fetchall()
        links = conn.execute(
            """SELECT nl.noun_id, nl.conversation_id, nl.visible_note_excerpt, nl.scene_type, nl.created_at, c.title
               FROM noun_links nl
               JOIN conversations c ON c.id = nl.conversation_id
               ORDER BY nl.created_at DESC"""
        ).fetchall()
    notes_by_noun: dict[str, list[dict]] = {}
    for link in rows_to_dicts(links):
        notes_by_noun.setdefault(link["noun_id"], []).append(link)
    nouns = []
    for noun in rows_to_dicts(rows):
        noun["linked_notes"] = notes_by_noun.get(noun["id"], [])
        nouns.append(noun)
    return {"nouns": nouns}

@app.get("/api/search")
def search(q: str = "", start_date: str | None = None, end_date: str | None = None):
    start_bound = _date_bound(start_date)
    end_bound = _date_bound(end_date, end=True)
    note_clauses = ["TRIM(COALESCE(user_note, '')) != ''"]
    note_params: list[str] = []
    noun_clauses: list[str] = []
    noun_params: list[str] = []

    if q:
        like = f"%{q}%"
        note_clauses.append("(user_note LIKE ? OR title LIKE ?)")
        note_params.extend([like, like])
        noun_clauses.append(
            """(n.noun LIKE ? OR EXISTS (
                   SELECT 1 FROM noun_links nl
                   WHERE nl.noun_id = n.id AND nl.visible_note_excerpt LIKE ?
               ))"""
        )
        noun_params.extend([like, like])
    if start_bound:
        note_clauses.append("created_at >= ?")
        note_params.append(start_bound)
        noun_clauses.append("updated_at >= ?")
        noun_params.append(start_bound)
    if end_bound:
        note_clauses.append("created_at < ?")
        note_params.append(end_bound)
        noun_clauses.append("updated_at < ?")
        noun_params.append(end_bound)

    note_where = " AND ".join(note_clauses)
    noun_where = " AND ".join(noun_clauses) if noun_clauses else "1=1"

    with connect() as conn:
        notes = conn.execute(
            f"SELECT id, created_at, title, user_note FROM conversations WHERE {note_where} ORDER BY created_at DESC",
            note_params
        ).fetchall()
        nouns = conn.execute(
            f"SELECT n.id, n.noun, n.updated_at FROM nouns n WHERE {noun_where} ORDER BY n.updated_at DESC",
            noun_params
        ).fetchall()
    return {
        "visible_results_only": True,
        "transcripts_saved_hidden": True,
        "notes": rows_to_dicts(notes),
        "nouns": rows_to_dicts(nouns)
    }

@app.get("/api/notes/extract")
def extract_user_notes(
    q: str = "",
    start_date: str | None = None,
    end_date: str | None = None,
    min_memos: int = Query(default=1, ge=1),
):
    rows = _query_note_rows(q=q, start_date=start_date, end_date=end_date, min_memos=min_memos)
    return {
        "notes_only": True,
        "transcripts_included": False,
        "audio_included": False,
        "note_count": len(rows),
        "notes": rows_to_dicts(rows),
    }

if __name__ == "__main__":
    uvicorn.run("app:app", host="0.0.0.0", port=8000, reload=False)
