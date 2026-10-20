CREATE TABLE exercise_progress (
    subject        TEXT    NOT NULL,
    exercise       TEXT    NOT NULL,
    attempts       INTEGER NOT NULL,
    last_verdict   TEXT,
    solved         INTEGER NOT NULL,
    hints_revealed INTEGER NOT NULL,
    updated_at     TEXT    NOT NULL,
    PRIMARY KEY (subject, exercise)
);

CREATE TABLE attempt (
    id       INTEGER PRIMARY KEY,
    subject  TEXT NOT NULL,
    exercise TEXT NOT NULL,
    at       TEXT NOT NULL,
    verdict  TEXT NOT NULL
);

CREATE TABLE session_finished (
    id            INTEGER PRIMARY KEY,
    subject       TEXT    NOT NULL,
    session       TEXT    NOT NULL,
    at            TEXT    NOT NULL,
    spent_seconds INTEGER NOT NULL
);
