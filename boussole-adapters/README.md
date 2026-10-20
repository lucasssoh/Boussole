# boussole-adapters

The outside world of `boussole-core`: subject folders, the SQLite file, `config.yaml`, YouTube.

| Package | Contents |
|---|---|
| `content` | `FileContentRepository`: the courses of the configured folders, each read again when one of its course files changed |
| `progress` | `SqliteProgressRepository` and `SqliteDatabase`, whose migrations are `src/main/resources/db/V*.sql` |
| `config` | `BoussoleConfigFile` (reads `config.yaml`, adds a folder without touching the rest) and `BoussolePaths` (per-user folders) |
| `video` | `YoutubeOEmbedCatalog`: whether a video exists, for `boussole validate --check-videos` |

A new migration is a new `V<n>__what.sql`, added to the list of `SqliteDatabase`. A migration
already released is never changed.

Tests use temporary folders, never the learner's own files.
