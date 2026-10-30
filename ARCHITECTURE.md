# Architecture

Boussole is a local web application: a Java server and an Angular interface, packaged as one
program that runs on the learner's machine. There are no accounts and no cloud. Courses are
text files written by people or by agents (see [`docs/FORMAT.md`](docs/FORMAT.md)); progress is
a SQLite database.

This document says where things are and why. Each module also has a short README saying what it
does and what it does not do.

Class diagrams of every module are in [`docs/diagrams/`](docs/diagrams/README.md).

---

## 1. Overview

```
            ┌──────────── browser ─────────────┐
            │  Angular (web/)                   │
            └───────────────┬──────────────────┘
                            │ REST + SSE (localhost)
┌───────────────────────────┴───────────────────────────────┐
│ boussole-server   Spring Boot: HTTP API, serves web/       │
│ boussole-cli      picocli: serve, open, remind, validate…  │
├────────────────────────────────────────────────────────────┤
│ boussole-adapters files, SQLite, system timers, YouTube    │
├────────────────────────────────────────────────────────────┤
│ boussole-core     the domain: learning, planning, reminders│
├────────────────────────────────────────────────────────────┤
│ boussole-format   course format: model, parser, validator  │
└────────────────────────────────────────────────────────────┘
```

Dependencies point **down only**. `boussole-format` and `boussole-core` use no framework: no
Spring, no JDBC, no HTTP. ArchUnit tests fail the build if this is broken.

---

## 2. Modules

### `boussole-format`

Knows the course format and nothing else, so it can be used on its own (the validator agents
run is this module).

- **Model**: immutable `record`s. A `Course` holds the `CourseSubject`, its `CourseSyllabus` (`SyllabusChapter`s
  of `SyllabusSession`s), its `CourseLexicon` of `LexiconTerm`s, and the `CourseSession`s read, each with its
  `SessionContent`. A session is a list of
  `Part`s (`TextPart`, `WarmupPart`, `NotionPart`, `CheckpointPart`, `PracticePart`), each a list of `Block`s.
  An exercise's `Answer` is a `SingleAnswer` (numeric, text), a `FillAnswer` of `FillGap`s, an
  `OrderAnswer` or a `MatchAnswer`; every value the learner gives is an `Expectation`: typed
  text, a number, or a choice. `Part`, `Block`, `Answer` and `Expectation` are sealed interfaces.
- **Loading**: `CourseLoader` reads the YAML files (SnakeYAML Engine, through `YamlMap`, which
  knows the line of every value) and each session file through `SessionFileReader`.
- **Parsing**: the structure of a session body is line-based, so Boussole parses it itself, in
  two steps. `RawTreeBuilder` turns lines into containers, one-line directives, headings and
  text, skipping fenced code; `BlockBuilder` checks each directive's kind, place and attributes,
  then hands it to its `DirectiveParser`, found in the `DirectiveParserRegistry`. Exercise answers are
  read by one `AnswerParser` per exercise type. Prose stays Markdown source: the interface
  renders it.
- **Validation**: what a file shows on its own is reported while it is read; what needs the
  whole course is checked by the `Validator`, which runs a list of `ValidationRule`s, one class
  per diagnostic code of `FORMAT.md` §6. `SubjectInspector` does both and returns an
  `InspectionReport`.

### `boussole-core`

The domain. Organised by feature:

```
core/
├── learn/       studying: use cases, ports
│   ├── check/      responses and how each kind of question checks them
│   ├── progress/   attempts, hints revealed, sessions finished
│   └── reward/     points, ranks and streak, computed from the history
├── plan/        scheduled events, the planner, commands with undo   (V2)
└── reminder/    reminder rules and planning of reminders            (V2)
```

It declares **ports**, the interfaces it needs from the outside world: `ContentRepository` and
`ProgressRepository` today; `EventRepository`, `ReminderScheduler` and `DomainEventPublisher`
with the planner. Time comes from a `java.time.Clock`.

Each thing the application does is a **use case**, one class with one public method:
`ListCourses`, `OpenCourse`, `OpenSession`, `SubmitAttempt`, `RevealNextHint`,
`RevealSolution`, `FinishSession`; later `PlanWeek`, `MoveEvent`… The list of use case classes
is the list of features.

Only what is right earns points. `SubmitAttempt` weighs each answer (`QuestionScore`: 1 for a
value, a single-choice MCQ or an order; 1 per sentence whose gaps are all right in a fill
(`GapSentences`); shares that wrong choices take back in a multiple-choice MCQ or a match; 0 for a
free exercise or a hook), multiplies it by the occasion (`PointRules`: the first answer whole,
half in another session that asks it again, nothing more in the same session) and by the level of the track
(`TrackLevel`), and keeps the points with the attempt. `FinishSession` adds a tenth, rounded up
last, the first time a session ends without a mistake. `ReadStudyStats` adds them up from the
history, and places them on two `Ladder`s (`Rank`): the culture, and the specialty of each field
(`FieldStanding`). Sage also asks for three specialties at Specialist or above; Grand Sage for
five besides critical thinking, and critical thinking itself (`Specialties`), a field of
Boussole's own. Tiers are split into divisions V to I of 14 levels; the top one never ends.
`TrackScores` tells what a track gives: every occasion a written session gives to answer a
question (its own whole, one asked again at half), what the first answer there earned and what it
missed; the page of a track shows it, earned, missed and still to play. `ReadAchievements`
counts the 19 `Achievement`s from the same history (each with levels I, II, III…, kept for
good, earning no points); the profile lists them.

In a lesson a question is answered once (`AnswerOnce`): `SubmitAttempt` refuses a second answer
in the same session, and `OpenSession` returns the verdict of each answer already given, which
the interface shows again (with the answer itself when the browser kept it). A review or a mock
exam can be sat again.

A response is a sealed `LearnerResponse` (choices, a value, gaps, an order, pairs, a self
grade). `QuestionChecker` hands it to the `ResponseChecker` of the question's kind, which
returns a `CheckResult`: a `Verdict` (correct, partly, wrong) and each part judged, never the
expected value.

### `boussole-adapters`

The implementations of the ports:

| Port | Adapter |
|---|---|
| `ContentRepository` | `FileContentRepository`: reads the folders of `config.yaml` with `boussole-format`, each again only when one of its course files changed (no watcher: a fingerprint of the files, compared on each request) |
| `ProgressRepository`, later `EventRepository` | `Sqlite…Repository`: plain JDBC; `SqliteDatabase` runs the migrations of `db/V*.sql` once each, counted by `PRAGMA user_version` |
| `ReminderScheduler` (V2) | `SystemdReminderScheduler`, `LaunchdReminderScheduler`, `WindowsTaskReminderScheduler` |
| `VideoCatalog` (of `boussole-format`) | `YoutubeOEmbedCatalog` |

`config/` reads and writes `config.yaml` (`BoussoleConfigFile`, which adds a folder without
touching the rest of the file) and knows the per-user folders (`BoussolePaths`).

### `boussole-server`

Spring Boot. Controllers turn HTTP into use case calls and nothing more. Spring wires the
adapters into the use cases here (`LearningConfiguration`), and only here. What the browser
receives are `…View` records; `BlockViewMapper` builds the blocks of a session and leaves out
every answer, hint not yet revealed and solution. Their TypeScript twins are in
`web/src/app/core/api.ts`.

The server listens on 127.0.0.1 only. `LocalRequestFilter` refuses a `Host` other than
localhost (another site pointed at 127.0.0.1) and a change without the `X-Boussole` header (a
form posted from another site). It also serves the interface, any other path answering with its
`index.html`.

The tracks built into Boussole are ordinary subjects, written in the course format under
`guide/<track>/<language>/` and copied into the jar: `getting-started`, the guide a new
Boussole opens on (its subject ID is `BundledTracks.STARTER`), and the tracks to learn more,
such as `course-format` (writing a track by hand). `BundledTracks` installs them, in every
language, into the data folder at start, and `ServerSettings.builtIn` lists them after the learner's subjects, always:
the home page shows the guide first under *How to Boussole* (and resumes it only while there is
no other subject), and the others on two shelves: *How to Boussole* for IDs starting with
`boussole-`, *Discover* for the others. They earn no points and keep no
streak (`RewardedSubjects`, in the core). `GuideSubjectTest` keeps every track valid in both
languages, with the same sessions.

### `boussole-cli`

picocli. One class per command.

| Command | Does |
|---|---|
| `boussole open [SUBJECT_DIR]` | Starts the server in the background if it is not running, then opens the browser, on the subject when one is given (added to `config.yaml` the first time). |
| `boussole serve` | Runs the server in the terminal, until Ctrl+C. |
| `boussole stop` | Stops the server. |
| `boussole init SUBJECT_DIR` | Creates `subject.yaml`, `cursus/` and the agent instructions. |
| `boussole validate SUBJECT_DIR [--check-videos]` | Runs the validator. |
| `boussole sources SUBJECT_DIR [--all]` | Lists the documents against `cursus/sources.yaml`: new, changed, pending, gone, with the entries to add. |
| `boussole index SUBJECT_DIR [--before SESSION]` | Lists every question of a subject, one per line, with how many times it is asked again (`::ref`): agents pick warmup and review questions there. |
| `boussole remind EVENT` (V2) | Shows the reminder of an event (called by system timers). |
| `boussole import-legacy …` (planned) | Imports the former Markdown sessions and Boussole settings. |

`spring-boot-maven-plugin` packages the commands, the server and the interface as one
executable `boussole.jar`. `bin/package` makes of it a program of its own for each system
(`jlink` for a smaller Java, `jpackage` for the program), which the release workflow publishes;
a packaged Boussole starts its server by running its own program again (`Launcher`).

### `web/`

Angular, built and tested by `frontend-maven-plugin` during the build of `boussole-server`,
into its resources. See §5.

### `skills/`

Instructions for agents (Agent Skills format, one `SKILL.md` per skill) that build a course from
the learner's documents. `boussole init` installs them in the subject folder with an `AGENTS.md`
that any agent can read. See §7.

---

## 3. Design patterns

A pattern is used where it solves a real problem, and the class name says which one it is.

| Pattern | Where | Why |
|---|---|---|
| Ports and adapters | between `core` and `adapters` | the domain knows neither Spring, nor SQLite, nor systemd |
| Use case | `core/*/` | one action = one class |
| Repository | ports of `core` | data access behind an interface |
| Strategy | `AnswerParser` per exercise type; `ResponseChecker` per kind of question; `ReminderScheduler` per OS | add a case without touching the others |
| Template method | `LineAnswerParser`: each answer type only says how a line reads and what the lines make | the reading loop and its error handling exist once |
| Registry + factory | `DirectiveParserRegistry`, `AnswerParserRegistry` | a new directive or exercise type is a new class, registered |
| Sealed interface + exhaustive `switch` | `Block`, `Answer`, `Expectation`, `LearnerResponse`, `BlockView`, `ScheduledEvent`; in `web/`, the `@switch` of `BlockListComponent` on the block type | the compiler lists every place a new type must be handled |
| Composite | `Validator` of `ValidationRule`s | one rule per file, tested alone |
| Command | planning actions (`MoveEvent`, `PinEvent`…) | undo and history |
| Observer (domain events) | `EventChanged` → `ReminderSync` | planning does not know reminders exist |
| Value object | `ExerciseKey`, `SessionKey`, `Rational`; later `EventId`, `TimeSlot`, `Offset` | no bare `String` or `long` travelling around |
| Facade | `LearnFacade` in `web/`, one per feature | components talk to one service |

---

## 4. Reminders

There is no separate kind of "notification". There are **scheduled events** (a study session,
an exam, a deadline, a class of the timetable, a task), and any of them can be reminded.

```java
public sealed interface ScheduledEvent
        permits StudySession, Exam, Deadline, TimetableClass, Task {
    EventId id();
    LocalizedText title();
    TimeSlot slot();
    String context();      // "RESEAUX · Adressage IPv4 · 60 min"
    DeepLink link();       // the page to open
}
```

```
ScheduledEvent ─► ReminderPolicy ─► ReminderPlanner ─► ReminderScheduler (port)
                  rules per event    applies rules,    sync(List<Reminder>)
                  kind, overridable  quiet hours,      ├─ systemd user timers
                  per event          daily cap         ├─ launchd
                                                       └─ Windows Task Scheduler
```

- No daemon: when the plan changes, `ReminderSync` writes system timers for the next 7 days.
  `sync` is idempotent: it compares the installed timers with the expected ones and changes
  only the difference.
- A timer runs `boussole remind <event>`, which shows one notification: title, context line,
  and an *Open* action running `boussole open <link>`.
- Without a supported scheduler, reminders are shown only while the interface is open.

**Durations are estimates, except for timed sessions.** The `duration` of a lesson is its content
timed at a normal pace; a lesson ends with its closing, never with a timer. Reviews and exams
(`kind: review`, `exam`) are timed: their duration is the time allowed, and their questions are
sized to fill it. The player records the time actually spent,
and the planner (V2) plans with the learner's measured pace, which varies from day to day,
rather than with the estimate alone.

---

## 5. Interface (`web/`)

```
web/src/app/
├── core/        BoussoleApi and the types of the API (api.ts), language, settings panel
├── i18n/        the interface in English and French, compiled in
├── learn/       subjects, course outline, session player, side panel (video, PDF)
│   ├── blocks/     BlockListComponent and one component per kind of block
│   └── question/   MCQ and exercises, sharing QuestionState and QuestionFooterComponent
├── plan/        today, week, deadlines, quick add                        (V2)
└── shared/      Markdown (markdown-it, KaTeX, :term, :pdf, gaps), Mermaid
```

- **The home page** puts on top the session to resume (in the subject studied last: the session
  answered in last, unless finished, else the next one whose `prerequisites` are finished), the sessions of its chapter, then every
  subject with its progress. A subject's page shows all its sessions as a grid, chapter by
  chapter (`SessionGridComponent`), each card with how many of its questions are answered and
  right (`SessionProgress`, computed by the server from the progress kept). A session whose
  `prerequisites` are not finished (`CourseOverview.waitingFor`) is locked in guided mode (it stays
  on its first screen, which links to them) and only advised after them in free mode.
- **The session player** shows **one screen at a time**, never the whole session
  (`sessionPlan()`): the goal, then within each part (warm-up, each notion, checkpoint,
  practice) the explanation, the worked example and each question on a screen of their own,
  then a closing screen. *Previous* and *Next* (or ← →) move between them. Beside it, the plan
  of the session drawn as a path (`SessionPathComponent`): a node per screen, winding down through
  a zone per part (warm-up, each notion, checkpoint, practice, each its own tint), opened by a
  sign on the line; the component computes every position, and zones and line are one SVG; the
  path scrolls back to the current screen at each change of screen, and may be scrolled by hand,
  its scroll bar on the left; green once seen, the colour of its verdict for a question, locked
  while a question before it waits for an answer; and the video or the document opened (the
  browser's own PDF viewer, at the page). The screen is in the address and, for the next
  visit, in this browser; `SessionRun` keeps the answers of the sitting. A lesson ends with
  *I have finished this session*; a review or a mock exam starts with its countdown and ends
  with the number of questions right at the first attempt. `SessionRun` measures the time
  spent while the page is visible.
- **A question** owns a `QuestionState`: hints revealed one at a time before answering, then
  **one answer**, which closes the question for the sitting. The server answers it with the
  verdict and the correction together: the right answer turns green, a wrong one red with its
  feedback, and `QuestionCorrectionComponent` shows *why* (the solution) beside the question,
  or below it on a narrow screen. In an exam, the correction waits for the end, and there is no
  hint. A free exercise shows its solution first, then the learner grades their answer. A fill
  exercise places its fields where the statement has `[[N]]` (`FillGaps`).

- **Standalone components and signals.** State lives in services, exposed as signals; no NgRx.
- **Container and presentational components.** A container gets data from its feature's
  facade; a presentational component only has inputs and outputs.
- **Translations** with Transloco, so the language changes without reloading. The interface is
  in English and French (the learner's choice, else `language:` of `config.yaml`, else the
  browser's); course content keeps its own language.
- **The look**: rounded, colourful; relief marks what can be pressed, and only that, the Nunito font bundled with the
  interface. Each subject has its colour (`subjectColor()`), set as `--subject` on the page.
  The header shows the rank (`RankEmblemComponent`), the level, the points and the streak
  (`StatsService`), and leads to the profile (`ProfilePageComponent`: the card, the route of
  divisions, the next one, the fields Sage asks for, each specialty) and to the tiers
  (`RanksPageComponent`: the culture, overall, beside one branch per field, its specialty from
  Novice to Luminary with where the learner stands); each answer shows what it was worth and the points it
  earned. A division higher than the one last seen on this browser (`rank.seen`) is shown once,
  full screen (`PromotionComponent`).
- **Bubbles**: entering a page or a screen, its elements pop in one after the other (the
  `bubble` keyframes, a rank `--i` per element). They animate `scale`, `translate` and
  `opacity`, never `transform`, so that centring and pressing are left alone; nothing moves when
  the system asks for reduced motion.
- **Icons** are SVG paths of `shared/icon.component.ts` (`<app-icon name="…">`, or `iconSvg()` in
  markup built outside templates). No emoji or pictographic character anywhere in the code.
- **One theme**, *Brume*, from CSS variables: a soft light ground for reading, the night kept for the places of the game (the high tiers, the achievements, a new division).

---

## 6. HTTP API (excerpt)

A track may exist in several languages: subjects with the same ID (FORMAT.md §9).
`ContentRepository.versions` lists them, `CourseVersions` picks the one asked for (`?lang=` on
every call that reads content, the attempt included, since a typed answer is in a language),
and progress stays keyed by ID, so it is shared. Answers, hints and solutions never reach the
browser before they are asked for. Boussole looks
at a web page only when the learner opens it, and never at an address of this machine or its
local network (`HttpWebPages`). Under `/api/subjects/{subject}`:

```
GET  /api/subjects?lang=                 tracks (once each, in lang when they have it), progress, the session to resume
GET  …?lang=                             chapters (the chapters each waits for), sessions (written, finished, the sessions of their chapter they need), lexicon, the languages of the track
GET  …/sessions/{session}                parts and blocks; questions without answers
POST …/sessions/{session}/finish         { spentSeconds }
POST …/exercises/{id}/attempts?session=  verdict, each part judged and the correction, recorded
POST …/exercises/{id}/hints              the next hint, recorded
GET  …/exercises/{id}/solution           after an attempt (at once for a free exercise)
GET  …/files/{path}                      a file of the subject folder: PDFs, videos (range requests), images
GET  …/lang/{lang}/files/{path}          the same, in one version of the track (the language in the path keeps relative links)
GET  /api/references/preview?url=        whether a web page a course cites may show beside the text
GET  /api/stats                          points, culture rank, specialty of each field, streak, records, route
GET  /api/ranks                          both ladders: every division, where it starts, its first level
GET  /api/achievements                   each achievement: the value counted, the levels reached, the values they ask
GET  /api/server                         lets boussole open recognise a running Boussole
POST /api/server/stop                    boussole stop
```

---

## 7. Agent skills

The application does not call any AI. Agents work on the files, the application reads them.
The skills shipped in `skills/`:

| Skill | Role |
|---|---|
| `learner-profile` | Asks the learner a few multiple-choice questions on how they learn, and writes `learner.yaml`. |
| `track-translate` | Writes a version of a track in another language next to it, translating text only and keeping every ID, so that progress and the place in a session carry over. |
| `cursus-discover` | With no documents (`boussole new`): asks what the learner wants, finds open sources on the web, downloads those whose licence allows it and sums up the others in source notes, records each with its address and licence, then hands over to `cursus-plan`. |
| `cursus-plan` | Reads `sources/`, writes `syllabus.yaml` with `status: proposed`, and stops for the learner. |
| `seance-write` | Writes one session following `FORMAT.md`, in the language of the sources. |
| `exercise-author` | Exercises and MCQs: feedback for each wrong choice, hints in tiers, answers recomputed. |
| `exam-build` | Reviews and mock exams: as many questions as the time allows, mostly MCQs, mirroring the past exams. |
| `lexique-build` | Terms and definitions for a learner who has never seen them. |
| `video-find` | Videos per notion, kept only if `boussole validate --check-videos` accepts them. |
| `sources-update` | Takes the documents that arrive or change into the course: new sessions with new IDs, never a renumbering; records each in `cursus/sources.yaml`. |
| `cursus-fix` | Runs `boussole validate` and fixes until there is no error. |

`boussole init` installs them in the subject folder:

| File | Owner | On a new `init` |
|---|---|---|
| `.boussole/skills/<skill>/SKILL.md`, `.boussole/FORMAT.md` | Boussole | rewritten, so skills follow the installed version |
| `AGENTS.md` | shared | only the block between `boussole:start` and `boussole:end` is replaced; the rest is the learner's |
| `CLAUDE.md` | shared | an `@AGENTS.md` line is added once, since Claude Code reads `CLAUDE.md` rather than `AGENTS.md` |
| `subject.yaml`, `cursus/syllabus.yaml` | the course | created when missing, never overwritten |
| `learner.yaml` | the learner | copied from `$XDG_CONFIG_HOME/boussole/learner.yaml` when missing and that file exists, never overwritten |

`AGENTS.md` gives the exact command that runs the validator of this installation
(the packaged program, or `java -jar …/boussole.jar validate .`), so agents run the right version even when another
program is called `boussole`.

A learner's path: run `boussole init` on the folder of documents, ask an agent to build the
course, read and approve the syllabus, let the agent write the sessions; the interface reloads
by itself.

---

## 8. Configuration and data

```yaml
# ~/.config/boussole/config.yaml
language: fr                   # interface: en | fr
port: 7420
free_navigation: false         # true: a lesson lets you go on without answering
tracks_dir: ~/Boussole          # where boussole new creates a track (default ~/Boussole)
subjects:
  - ~/courses/mim-m1/RESEAUX
  - ~/courses/mim-m1/OC
reminders:
  quiet_hours: "22:15-07:00"
  daily_cap: 3
```

| What | Where |
|---|---|
| Configuration | `$XDG_CONFIG_HOME/boussole/config.yaml` |
| Progress, plan, history | `$XDG_DATA_HOME/boussole/state.db` |
| Courses | the subject folders listed in the configuration, never inside Boussole |
| Built-in tracks | `$XDG_DATA_HOME/boussole/guide/<track>/<language>/`, rewritten at each start |

On macOS and Windows the equivalent per-user folders are used.

---

## 9. Conventions

1. **Packages by feature, not by layer**: everything about reminders is in `reminder/`.
2. **Names say the role**: a use case is named by its verb (`SubmitAttempt`); every other role
   has its suffix: `…Repository`, `…Checker`, `…Rule`, `…Scheduler`, `…Facade`, `…Component`,
   `…Controller`, `…Command`.
3. **Names say the hierarchy**, so that listing a package is enough to understand it:
   - a subtype ends with its supertype's name: `VideoBlock` is a `Block`, `NotionPart` a `Part`,
     `ExampleDirectiveParser` a `DirectiveParser`, `NumberExpectation` an `Expectation`;
   - a part of something is prefixed by its owner: `CourseSubject`, `CourseLexicon`,
     `LexiconTerm`, `SyllabusChapter`, `ExerciseHint`, `McqChoice`, `FillGap`;
   - nested types (`RawNode.Text`, `AcceptedText.Exact`) are kept for the small closed variants
     of one type.
4. **Immutable by default**: `record`s, unmodifiable collections. No `null` in the domain;
   `Optional` only as a return type.
5. **An interface needs a reason**: a port to the outside world, or at least two
   implementations.
6. **Small classes, short methods**, and names that make comments unnecessary. Comments explain
   *why*, never *what*.
7. **Tests**: JUnit 6 and AssertJ. The parser is tested on reference files
   (`examples/` and `boussole-format/src/test/resources`); each validation rule has its own test
   class; ArchUnit checks the module dependencies.
8. No emoji in the code, the interface or the command output: icons are SVG (§5), words say the rest.
9. Code, comments, commits and documentation in English. Commits, versions and releases follow
   [`CONTRIBUTING.md`](CONTRIBUTING.md).
10. **Docs follow the code, in the same commit.** [`AGENTS.md`](AGENTS.md) has a recipe per kind of
    change (block, exercise type, diagnostic code, use case, command, interface text) listing
    every file it touches; `DocumentationSyncTest` (in `boussole-cli`) fails the build when the
    format, the diagrams or the interface types no longer match the code.

---

## 10. Stack

| | |
|---|---|
| Java | 25 (LTS), Maven multi-module |
| Server | Spring Boot 4 |
| Persistence | SQLite (xerial JDBC), migrations run by `SqliteDatabase` |
| Course files | own line-based parser for the structure, SnakeYAML Engine, PDFBox for page counts; Markdown rendered by markdown-it in `web/` |
| CLI | picocli |
| Interface | Angular 22 (standalone components, signals), Transloco, markdown-it, KaTeX, Mermaid |
| Tests | JUnit 6, AssertJ, ArchUnit; Vitest for `web/` |

---

## 11. Roadmap

1. ✓ `boussole-format`: model, parser, validator, tested on `examples/`.
2. ✓ Server and session player: blocks, exercises, hints, video, PDF.
3. ✓ Skills and `boussole init`, tried on a full subject (OC, 25 sessions).
4. `boussole import-legacy`.
5. V2: scheduled events, planner, reminders, timetable (iCal).
6. V3: spaced repetition, notes and highlights, code exercises, statistics.
