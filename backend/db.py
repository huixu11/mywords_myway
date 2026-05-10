import os
import sqlite3
from contextlib import contextmanager
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DB_PATH = Path(os.getenv("APP_DB_PATH", ROOT / "data" / "app.db"))
SCHEMA_PATH = ROOT / "schema.sql"

@contextmanager
def connect():
    DB_PATH.parent.mkdir(parents=True, exist_ok=True)
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    conn.execute("PRAGMA foreign_keys = ON")
    try:
        yield conn
    except Exception:
        conn.rollback()
        raise
    else:
        conn.commit()
    finally:
        conn.close()

def init_db():
    with connect() as conn:
        schema = SCHEMA_PATH.read_text(encoding="utf-8")
        conn.executescript(schema)
        _ensure_column(conn, "voice_memos", "audio_deleted_at", "audio_deleted_at TEXT")
        _ensure_column(conn, "voice_memos", "transcript_deleted_at", "transcript_deleted_at TEXT")
        _ensure_column(conn, "conversations", "payment_status", "payment_status TEXT DEFAULT 'free_weekly'")
        _ensure_column(conn, "conversations", "is_free_weekly", "is_free_weekly INTEGER DEFAULT 0")
        conn.commit()

def rows_to_dicts(rows):
    return [dict(row) for row in rows]

def _ensure_column(conn, table, column, ddl):
    columns = {row["name"] for row in conn.execute(f"PRAGMA table_info({table})").fetchall()}
    if column not in columns:
        conn.execute(f"ALTER TABLE {table} ADD COLUMN {ddl}")
