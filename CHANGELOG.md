# Changelog

Versions follow `MAJOR.MINOR.PATCH`, and each release is a git tag (`v1.0.0`). Major releases
also get a code name, minor ones their date; patch releases have neither. Until `1.0.0`, a
minor release may break things and says so.

## Unreleased

### Added
- **Course format, validation:** `boussole validate <folder>` checks a subject against the course format and lists each problem with its file, line and what to change. It ends with `0` when there is no error, so an agent writing a course can run it until it passes.
