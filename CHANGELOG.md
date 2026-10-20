# Changelog

Versions follow `MAJOR.MINOR.PATCH`, and each release is a git tag (`v1.0.0`). Major releases
also get a code name, minor ones their date; patch releases have neither. Until `1.0.0`, a
minor release may break things and says so.

## Unreleased

### Added
- **Course format:** options of a choice gap and right-hand sides of a match show in drop-down lists, which cannot draw maths: write them in plain text with Unicode symbols (≤, x₁), not `$…$`.
- **Course format, validation:** `boussole validate <folder>` checks a subject against the course format and lists each problem with its file, line and what to change. It ends with `0` when there is no error, so an agent writing a course can run it until it passes.
- **Agents:** `boussole init <folder>` turns a folder of course documents into a subject ready for an AI agent (Claude Code, Codex, Gemini CLI…): it creates the subject's files and installs instructions that guide the agent from the plan of the course, which you approve, to each session, its exercises, its lexicon and its videos. Running it again updates the instructions and never touches your course or your documents.
- **Agents, your way of learning:** before anything else, the agent asks you a few multiple-choice questions (a quick quiz after each point? try before the example? close with a quiz, an exercise or both? how long a session?) and every session is then built that way. `boussole validate` warns when a session does not follow your answers. Kept in `~/.config/boussole/learner.yaml`, your answers are reused by `boussole init` for your other subjects.
- **Fill-in exercises:** a gap can now be a drop-down choice or a number, besides typed text. Proofs, algorithms and equations are asked as fill-ins where you pick each missing step, so you never have to type a formula.
- **Session player:** `boussole open <folder>` opens your subject in the browser. A session shows its goal and objectives, its notions, maths and diagrams; videos and your own PDFs (at the right page) open beside the text, and terms of the lexicon show their definition on hover. You answer MCQs by ticking, fill gaps by typing or picking, order and match items, and get the feedback of each wrong choice; hints come one at a time, and the solution once you have tried. Your progress is kept, and the course shows the sessions you have finished and where to continue.
- **Session player:** a session comes one page at a time (warm-up, each notion, checkpoint, practice), and each page step by step, a question at a time: *Continue* shows what follows, *Previous* and *Next* (or ← and →) turn the pages, and your answers stay when you come back.
- **Session player:** you answer a question once; right after, the right answer turns green, yours red if it was wrong (with why), and a « Why » block beside the question explains the right answer. Before answering, you can reveal hints one at a time. In a mock exam, corrections come when the time is up or when you end it.
- **Course format, validation:** every MCQ needs hints too (P004), and every wrong choice of an MCQ its feedback (P010), since the learner sees them right after answering.
- **Session player, reviews and mock exams:** a timed session starts with its countdown and ends with the number of questions you got right at the first attempt.
- **Session player:** English or French, light or dark, and usable on a phone.
- **Server:** `boussole open` starts it in the background when needed, `boussole stop` stops it (even when it no longer answers), `boussole serve` runs it in a terminal. It only answers this machine, and keeps working while a new version of Boussole is built.
- **Course format, validation:** `boussole validate --check-videos` also asks YouTube whether each video exists.
- **Reviews and mock exams:** besides lessons, a course has timed sessions: a short review at the end of each chapter, and mock exams as long as the real ones. Their number of questions fits the time allowed, knowing that choosing an answer is quicker than building one; `boussole validate` warns when it does not.
