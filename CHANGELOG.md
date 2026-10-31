# Changelog

Versions follow `MAJOR.MINOR.PATCH`, and each release is a git tag (`v1.0.0`). Major releases
also get a code name, minor ones their date; patch releases have neither. Until `1.0.0`, a
minor release may break things and says so.

## Unreleased

### Added
- **`boussole` alone (beta):** the server, the devices and your tracks, full screen in the terminal. Start, stop or restart the server and turn network access on or off; accept a device's request or show a code and its QR code; see each track's problems and new documents, add a folder or take one off the list.
- **Other devices:** with `network: true` in `config.yaml`, a phone, a tablet or a TV on the same network can open Boussole once paired: `boussole devices pair` shows a QR code and a code, or the device shows a code to accept with `boussole devices accept`. Without pairing, the network gets nothing. A device may be read-only; `boussole devices remove` takes one away.
- **Stars:** each answer now shows up to 3 stars for how much of it was right: in the correction, under its step on the Trail, in the wrap-up and on each session card. A question met before (a warmup, a quick review) shows blue stars that do not count in the total; a free exercise shows your own estimate, outlined.

### Fixed
- **On a phone:** the Trail opens over the step with a ground of its own, below the top bar; Previous and Next sit in a bar of their own; the part's name leaves the bar above the step, the Trail shows it; the profile's gauge sits above its words.
- **Free exercises:** self-grading no longer awards points, and the answer is labelled as a proposed correction.
- **Fill-in exercises:** typing a number no longer closes the field after each digit.

## 0.2.1

### Fixed
- **Fill-in exercises:** long correct answers now wrap instead of running past the edge of the page.

## 0.2.0 (October 26 2026)

### Added
- **Documents:** Markdown files open rendered beside the lesson instead of downloading.

### Fixed
- **Tables:** wide tables in lessons scroll when needed; in solutions, they stay inside their card and scroll there. After your answer, an off-screen solution is brought into view.

## 0.1.1

### Fixed
- **macOS release:** application archives can now be built for versions before 1.0.0.

## 0.1.0 (October 26 2026)
### Added
- **The documents of a session, in one place:** the **Documents** button, above the step, lists every page of your documents, site and video the session cites, and marks those of the step you are on. A click opens it on the right; there, the arrow goes back to the list, the title switches to another document, and **Close** (or Escape) closes the panel.
- **The points you earn, as you earn them:** after a right answer, a « +3 » rises beside your tier, at the top right, and fades.

### Fixed
- **A session that stayed blank,** its steps never showing and the needle turning for good: it opens again.
- **I have finished this session** no longer comes back on a session you have already finished: its last step says you have.

### Changed
- **Licence of the contents:** the built-in tracks, the playbooks, the course format and the research notes are shared under CC BY-NC-SA 4.0; the software keeps its licence.
- **Your tier, at the top right,** shows your points and the next mark (`180 / 240`) rather than the bounds of the division (`0 → 240`).
- **Tables** take the width they need, beyond the column of text when the screen has room, centred, and never cut a formula in two; only a screen too narrow for them scrolls them.
- **Definitions of the lexicon** show above everything, tables and frames included, and stay inside the window.
- **Drop-down lists** in exercises (pairing, and choices in a sentence to fill) open in the colours and the font of Boussole, rather than in those of the browser or of the system (a black list on a dark desktop).

## 0.0.2

### Added
- **Boussole ready to run:** each release comes as an archive for Linux, macOS and Windows, with its own Java inside: extract it and run `boussole`, nothing else to install.

### Fixed
- **The version of Boussole** shown by the interface is the version you run, rather than « dev ».

## 0.0.1 (October 25 2026)

The starting point: what Boussole does in its first release.

### Tracks from your documents
- **`boussole init <folder>`** turns a folder of course documents into a track ready for an AI agent (Claude Code, Codex, Gemini CLI…), with playbooks that guide it from the plan, which you approve, to each session, its exercises, its lexicon and its videos. Running it again updates the playbooks and never touches your course or your documents.
- **Your way of learning:** before anything else, the agent asks you a few questions (a quick quiz after each point? try before the example? how long a session?), and every session is built that way. Your answers are kept for your other tracks.
- **`boussole new <name> [--goal …]`** starts a track without documents of your own: the agent asks what you want to be able to do, finds open sources (official documentation, university courses, open textbooks), records each with its address and licence, shows you the list, then makes the plan.
- **Documents that arrive during the year:** the document tracking records what the track did with each document. `boussole sources <folder>` lists those that are new, changed or gone, and the agent takes them in without rewriting what is written.
- **The validator,** `boussole validate <folder>`, checks a track against the course format and lists each problem with its file, line and what to write instead; the agent runs it until it passes. `--check-videos` also asks YouTube whether each video exists.
- **`boussole index <folder>`** lists every question of a track with how often it is asked again elsewhere, so that the agent varies warmups and reviews.
- **Tracks in several languages:** the `track-translate` playbook writes a version of a track in another language, translating only the text; the validator compares the versions.

### The course format
- **A track** is a plan (chapters and sessions), sessions in Markdown, a lexicon and its sources. `subject.yaml` says how demanding it is (discovery to research) and its field (from the UNESCO classification), or critical thinking, a field of Boussole's own.
- **Sessions** have a goal, objectives, notions, worked examples, reminders and callouts, maths, `:::graph` diagrams, Mermaid diagrams, PDF pages, videos (YouTube or your own recordings) and web pages opened beside the text.
- **Questions:** MCQs with one or several right answers, values, fill-ins (typed, numbers or drop-down choices, so that a proof or an algorithm is filled step by step without typing a formula), ordering, matching and free exercises, each with nudges, a feedback per wrong choice and an explained solution.
- **Warmups and reviews:** a warmup asks again questions of earlier sessions, and may ask a hook question, a guess before the lesson. Quick reviews close each chapter, mock exams last as long as the real ones.
- **Prerequisites:** a session can name the sessions it really needs; a prerequisite in another chapter makes the whole chapter wait for that chapter.
- **Sources and credits:** each notion cites where it comes from, and `:::beyond` sets apart what does not come from the documents. Each session says who wrote it (a person, or an AI model and its tool, with the date); a track credits its authors and contributors.

### Studying
- **`boussole open <folder>`** opens a track in the browser; `--resume` opens the session to resume, for a reminder in your calendar.
- **A session, one step at a time:** its goal, then each explanation, example and question, then a wrap-up. The Trail on the left shows each part and each step, green, orange or red as you answer.
- **First try:** in a lesson, your first answer is final, and right after it comes the explained correction. Nudges come one at a time before you answer; an MCQ with several right answers asks for a final check. In guided mode, *Next* waits for your answer.
- **Quick reviews and mock exams** are timed; a mock exam keeps its corrections for the end.
- **MCQ choices** are shuffled the same way each time, so that the place of the right one never gives it away.
- **Ordering** by dragging with the mouse or a finger, or with arrows; each line keeps its colour.

### Tracks and their map
- **Home:** the session to resume first, then your tracks, and the tracks built into Boussole on two shelves: *How to Boussole* and *Discover*.
- **The page of a track:** what it is, how demanding, its score (earned, missed, still to play), *Start* or *Resume*, its credits and lexicon, beside its map.
- **The map:** chapters as blocks linked by what comes before what, each in its colour; a click zooms into a chapter, its sessions linked the same way. Finished blocks are checked, the one to go on with stands out, those not open yet are locked. It fits beside the track, on graph paper; a track of one chapter opens on it.
- **Several languages:** the versions of a track show as one; pick the language on its page or switch in a session, at the same place. Progress is the same in every language.

### Points and ranks
- **Points:** only what is right earns, multiplied by how demanding the track is; a question asked again earns half, a session without a mistake a tenth more.
- **Two ladders:** the culture, from Curious to Sage in divisions V to I, then Grand Sage without end, 70 levels a tier; and a rank in each field, from Novice to Luminary. Sage asks for three fields at Specialist, Grand Sage for five and critical thinking.
- **Emblems:** each tier has its own, a gem around an instrument of navigation, ornamented and lit more as you rise; each field rank a medallion in a more noble metal.
- **Your pill,** in the header: your emblem, your level and the bounds of your division, filling up as you go, in a frame of your tier. A new division or a new tier is shown once the session is over: the numeral changes, or the emblem comes apart and is put together again.
- **Your profile:** your emblem, your records, the route of the divisions, your fields as pills, and 19 achievements with their levels. **The tiers page:** the eight tiers on one line, and what Sage and Grand Sage ask, with where you stand.
- **Your streak** of days in a row.

### Built into Boussole
- **Getting started with Boussole,** the track a new Boussole opens on: what Boussole does, preparing a track with an agent, studying a session, documents that arrive later.
- **Writing a track by hand:** the course format step by step, with one example built across the sessions.
- **Rights and credits:** who owns a course document, reading a licence, citing, the credits Boussole shows, sharing a track. General information, not legal advice.

### The interface
- **One theme, Brume:** a soft light ground for reading, the green of Boussole for what you do, the night kept for the high tiers.
- **English or French,** usable on a phone; pages fade into one another, a page that waits shows the needle of Boussole turning, and nothing moves when your system asks for less motion.
- **The server** runs on your machine only: `boussole open` starts it when needed, `boussole stop` stops it, `boussole serve` runs it in a terminal.
