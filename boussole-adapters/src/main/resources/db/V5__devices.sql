-- The devices paired with this Boussole. Only the hash of each device's key is kept: the key
-- itself is given once to the device, and a stolen copy of this file opens nothing.
CREATE TABLE device (
    id        TEXT PRIMARY KEY,
    name      TEXT NOT NULL,
    address   TEXT NOT NULL,
    access    TEXT NOT NULL,
    key_hash  TEXT NOT NULL UNIQUE,
    paired_at TEXT NOT NULL,
    last_seen TEXT NOT NULL
);
