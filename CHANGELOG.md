# Changelog

Versions follow `MAJOR.MINOR.PATCH`, and each release is a git tag (`v1.0.0`). Major releases
also get a code name, minor ones their date; patch releases have neither. Until `1.0.0`, a
minor release may break things and says so.

## Unreleased

### Added
- **Course format, validation:** `boussole validate <folder>` checks a subject against the course format and lists each problem with its file, line and what to change. It ends with `0` when there is no error, so an agent writing a course can run it until it passes.
- **Agents:** `boussole init <folder>` turns a folder of course documents into a subject ready for an AI agent (Claude Code, Codex, Gemini CLI…): it creates the subject's files and installs instructions that guide the agent from the plan of the course, which you approve, to each session, its exercises, its lexicon and its videos. Running it again updates the instructions and never touches your course or your documents.
- **Agents, your way of learning:** before anything else, the agent asks you a few multiple-choice questions (a quick quiz after each point? try before the example? close with a quiz, an exercise or both? how long a session?) and every session is then built that way. `boussole validate` warns when a session does not follow your answers. Kept in `~/.config/boussole/learner.yaml`, your answers are reused by `boussole init` for your other subjects.
- **Fill-in exercises:** a gap can now be a drop-down choice or a number, besides typed text. Proofs, algorithms and equations are asked as fill-ins where you pick each missing step, so you never have to type a formula.
- **Reviews and mock exams:** besides lessons, a course has timed sessions: a short review at the end of each chapter, and mock exams as long as the real ones. Their number of questions fits the time allowed, knowing that choosing an answer is quicker than building one; `boussole validate` warns when it does not.
