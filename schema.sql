PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS conversations (
    id TEXT PRIMARY KEY,
    created_at TEXT NOT NULL,
    title TEXT,
    user_note TEXT,
    payment_status TEXT DEFAULT 'free_weekly',
    is_free_weekly INTEGER DEFAULT 0,
    safety_status TEXT DEFAULT 'none'
);

CREATE TABLE IF NOT EXISTS voice_memos (
    id TEXT PRIMARY KEY,
    conversation_id TEXT NOT NULL,
    audio_path TEXT,
    audio_deleted_at TEXT,
    transcript TEXT,
    transcript_deleted_at TEXT,
    created_at TEXT NOT NULL,
    visible_to_user INTEGER DEFAULT 0,
    FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS nouns (
    id TEXT PRIMARY KEY,
    noun TEXT NOT NULL,
    user_meaning TEXT DEFAULT '',
    status TEXT NOT NULL DEFAULT 'confirmed',
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS noun_links (
    id TEXT PRIMARY KEY,
    noun_id TEXT NOT NULL,
    conversation_id TEXT NOT NULL,
    memo_id TEXT,
    transcript_span TEXT,
    visible_note_excerpt TEXT,
    scene_type TEXT,
    created_at TEXT NOT NULL,
    FOREIGN KEY (noun_id) REFERENCES nouns(id) ON DELETE CASCADE,
    FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS noun_suggestions (
    id TEXT PRIMARY KEY,
    conversation_id TEXT NOT NULL,
    suggested_noun TEXT NOT NULL,
    suggestion_type TEXT NOT NULL,
    visible_reason TEXT,
    evidence_span TEXT,
    scene_type TEXT,
    confidence TEXT,
    status TEXT NOT NULL DEFAULT 'pending',
    created_at TEXT NOT NULL,
    FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE
);
