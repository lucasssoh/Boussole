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
├── learn/       exercises, attempts, hints, mastery, spaced repetition
├── plan/        scheduled events, the planner, commands with undo   (V2)
├── reminder/    reminder rules and planning of reminders            (V2)
└── shared/      IDs, time, localized text, domain events
```

It declares **ports**, the interfaces it needs from the outside world: `ContentRepository`,
`ProgressRepository`, `EventRepository`, `ReminderScheduler`, `Clock`, `DomainEventPublisher`.

Each thing the application does is a **use case**, one class with one public method:
`SubmitAttempt`, `RevealNextHint`, `RevealSolution`, `OpenSession`, `PlanWeek`, `MoveEvent`…
The list of use case classes is the list of features.

### `boussole-adapters`

The implementations of the ports:

| Port | Adapter |
|---|---|
| `ContentRepository` | `FileContentRepository`: reads subjects with `boussole-format`, watches the folders and reloads on change |
| `ProgressRepository`, `EventRepository` | `Sqlite…Repository`: JDBC, schema migrated with Flyway |
| `ReminderScheduler` | `SystemdReminderScheduler`, `LaunchdReminderScheduler`, `WindowsTaskReminderScheduler` |
| `VideoChecker` | `YoutubeOEmbedChecker` |

### `boussole-server`

Spring Boot. Controllers turn HTTP into use case calls and nothing more. Spring wires the
adapters into the use cases here, and only here. The OpenAPI description of the API is
generated (springdoc) and the TypeScript client of `web/` is generated from it, so the Java
and TypeScript types cannot drift apart.

### `boussole-cli`

picocli. One class per command.

| Command | Does |
|---|---|
| `boussole serve` | Starts the server. |
| `boussole open [PATH]` | Starts the server if it is not running, then opens the browser on `PATH`. |
| `boussole remind EVENT` | Shows the reminder of an event (called by system timers). |
| `boussole init SUBJECT_DIR` | Creates `subject.yaml`, `cursus/` and the agent instructions. |
| `boussole validate SUBJECT_DIR [--check-videos]` | Runs the validator. |
| `boussole import-legacy …` | Imports the former Markdown sessions and Boussole settings. |

### `web/`

Angular, built by `frontend-maven-plugin` into the server's static resources. See §5.

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
| Strategy | `AnswerParser` and `AnswerChecker` per exercise type; `ReminderScheduler` per OS | add a case without touching the others |
| Template method | `LineAnswerParser`: each answer type only says how a line reads and what the lines make | the reading loop and its error handling exist once |
| Registry + factory | `DirectiveParserRegistry`, `AnswerParserRegistry`; `BLOCK_RENDERERS` in `web/` | a new block type is a new class, registered |
| Sealed interface + exhaustive `switch` | `Block`, `ScheduledEvent` | the compiler lists every place a new type must be handled |
| Composite | `Validator` of `ValidationRule`s | one rule per file, tested alone |
| Command | planning actions (`MoveEvent`, `PinEvent`…) | undo and history |
| Observer (domain events) | `EventChanged` → `ReminderSync` | planning does not know reminders exist |
| Value object | `EventId`, `ExerciseId`, `TimeSlot`, `LocalizedText`, `Offset` | no bare `String` or `long` travelling around |
| Facade | one per feature in `web/` | components talk to one service |

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
├── core/        injectable services: generated API client, I18nService, ThemeService
├── learn/       course tree, session player, side panel (video, PDF)
│   └── blocks/  one component per block type, found through BLOCK_RENDERERS
├── library/     PDF reader (pdf.js), search, plain Markdown reading
├── plan/        today, week, deadlines, quick add                        (V2)
└── shared/      KaTeX, Mermaid, code highlighting, lexicon tooltips
```

- **Standalone components and signals.** State lives in services, exposed as signals; no NgRx.
- **Container and presentational components.** A container gets data from its feature's
  facade; a presentational component only has inputs and outputs.
- **Translations** with Transloco, so the language changes without reloading. The interface is
  in English and French; course content keeps its own language.

---

## 6. HTTP API (excerpt)

Answers, hints and solutions never reach the browser before they are asked for:

```
GET  /api/subjects                              subjects and progress
GET  /api/sessions/{id}                         blocks; exercises without answers
POST /api/exercises/{id}/attempts               { correct, feedback }, recorded
POST /api/exercises/{id}/hints/next             the next hint, recorded
GET  /api/exercises/{id}/solution               only after an attempt
GET  /api/sources/{subject}/{file}              a learner's document (PDF)
GET  /api/events/stream                         SSE: content reloaded, reminders
```

---

## 7. Agent skills

The application does not call any AI. Agents work on the files, the application reads them.
The skills shipped in `skills/`:

| Skill | Role |
|---|---|
| `learner-profile` | Asks the learner a few multiple-choice questions on how they learn, and writes `learner.yaml`. |
| `cursus-plan` | Reads `sources/`, writes `syllabus.yaml` with `status: proposed`, and stops for the learner. |
| `seance-write` | Writes one session following `FORMAT.md`, in the language of the sources. |
| `exercise-author` | Exercises and QCMs: feedback for each wrong choice, hints in tiers, answers recomputed. |
| `exam-build` | Reviews and mock exams: as many questions as the time allows, mostly QCMs, mirroring the past exams. |
| `lexique-build` | Terms and definitions for a learner who has never seen them. |
| `video-find` | Videos per notion, kept only if `boussole validate --check-videos` accepts them. |
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
(`java -jar …/boussole.jar validate .`), so agents run the right version even when another
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
     `LexiconTerm`, `SyllabusChapter`, `ExerciseHint`, `QcmChoice`, `FillGap`;
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
8. Code, comments, commits and documentation in English. Commits, versions and releases follow
   [`CONTRIBUTING.md`](CONTRIBUTING.md).

---

## 10. Stack

| | |
|---|---|
| Java | 25 (LTS), Maven multi-module |
| Server | Spring Boot 3, springdoc-openapi |
| Persistence | SQLite (xerial JDBC), Flyway |
| Course files | own line-based parser for the structure, SnakeYAML Engine, PDFBox for page counts; Markdown rendered by markdown-it in `web/` |
| CLI | picocli |
| Interface | Angular (standalone components, signals), Transloco, pdf.js, KaTeX, Mermaid |
| Tests | JUnit 6, AssertJ, ArchUnit; Vitest and Playwright for `web/` |

---

## 11. Roadmap

1. `boussole-format`: model, parser, validator, tested on `examples/`.
2. Server and session player: blocks, exercises, hints, video, PDF.
3. Skills and `boussole init`, tried on a full subject.
4. `boussole import-legacy`.
5. V2: scheduled events, planner, reminders, timetable (iCal).
6. V3: spaced repetition, notes and highlights, code exercises, statistics.
