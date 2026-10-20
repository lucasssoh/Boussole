# Changelog

Versions follow `MAJOR.MINOR.PATCH`, and each release is a git tag (`v1.0.0`). Major releases
also get a code name, minor ones their date; patch releases have neither. Until `1.0.0`, a
minor release may break things and says so.

## Unreleased

### Added
- **Course format, validation:** `boussole validate <folder>` checks a subject against the course format and lists each problem with its file, line and what to change. It ends with `0` when there is no error, so an agent writing a course can run it until it passes.
- **Agents:** `boussole init <folder>` turns a folder of course documents into a subject ready for an AI agent (Claude Code, Codex, Gemini CLI…): it creates the subject's files and installs instructions that guide the agent from the plan of the course, which you approve, to each session, its exercises, its lexicon and its videos. Running it again updates the instructions and never touches your course or your documents.
