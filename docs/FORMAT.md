# Boussole course format — version 1

This document is the contract between three readers:

- **people** who write or correct a course in a text editor;
- **agents** that turn a learner's documents (lecture notes, slides, past exams) into a course;
- **Boussole**, which parses, validates and renders it.

If this document and the code disagree, the code has a bug.

Principles:

1. **Plain text only.** A course is Markdown and YAML files that can live in git.
2. **One file = one session** of 45 to 90 minutes.
3. **Stable identifiers.** Progress is stored against IDs, never against file names or positions.
4. **Answers stay hidden.** Answers, hints and solutions are never sent to the browser before the
   learner asks for them or makes an attempt.
5. **The course speaks the language of its sources.** The interface is bilingual (en/fr); the
   content is in the language declared by the subject.

---

## 1. Layout of a subject

A *subject* is the format's name for one course of any kind: a school subject, but also a
software manual, a company guide or a programming language. The interface calls it a course.
A subject sits next to the learner's own documents. Its folder is declared in Boussole's
configuration (see `ARCHITECTURE.md`, *Configuration*).

```
<subject>/
├── subject.yaml              # what the subject is
├── learner.yaml              # how the learner learns best (optional, §7)
├── sources/                  # the learner's documents (PDF…), never modified by Boussole
└── cursus/
    ├── syllabus.yaml         # chapters and sessions, in order
    ├── lexique.yaml          # the terms of the subject
    ├── sources.yaml          # what the course did with each document (§8)
    ├── assets/               # images used by sessions
    └── <chapter dir>/
        └── <session file>.md
```

`boussole init` also writes `.boussole/` (this document and the agent skills), `AGENTS.md` and
`CLAUDE.md`, for the agents that build the course. Files and folders that Boussole does not know
are ignored, so a subject can hold anything else.

---

## 2. `subject.yaml`

```yaml
format: 1
id: reseaux
name: Réseaux
lang: fr
description: M1 networking course, network and transport layers.
```

| Field | Required | Meaning |
|---|---|---|
| `format` | yes | Version of this format. Always `1` for now. |
| `id` | yes | Slug: lowercase letters, digits and `-`. Unique among the learner's subjects. |
| `name` | yes | Display name. |
| `lang` | yes | BCP 47 tag (`fr`, `en`, `de`…): the language of the sources, hence of the course. |
| `sources` | no | Folder of the learner's documents, relative to the subject. Default `sources`. |
| `description` | no | One or two sentences. |
| `authors` | no | Who makes and signs the track: people or organisations. Each a `name`, optionally `organisation: true` and a `url`. |
| `contributors` | no | Who helped: each a `name`, optionally a `role` (`exercises of chapter 3`, `review`) and a `url`. |

---

## 3. `cursus/syllabus.yaml`

The syllabus is the plan of the course. An agent writes it first with `status: proposed`; the
learner reads it, edits it if needed, and sets `status: approved`. Sessions are written only
after that.

```yaml
status: approved              # proposed | approved
chapters:
  - id: couche-reseau
    title: La couche réseau
    dir: 01-couche-reseau
    sources: [Reseaux_Cours_Intro_2026.pdf, Reseaux_TD.pdf]
    sessions:
      - id: s11
        title: Rôle de la couche réseau
        duration: 60
        goal: Dire ce que la couche 3 apporte par rapport à la couche 2.
        file: 11-role-couche-reseau.md
      - id: r19
        title: Révision — la couche réseau
        kind: review
        duration: 20
        goal: Retrouver l'essentiel de la couche réseau sans notes, en temps limité.
        file: 19-revision.md
      - id: s12
        title: RIB, FIB et plus long préfixe
        duration: 60
        goal: Lire une table de routage et choisir la bonne route.
        file: 12-rib-fib-lpm.md
```

| Field | Required | Meaning |
|---|---|---|
| `status` | yes | `proposed` or `approved`. |
| `chapters[].id` | yes | Slug, unique in the subject. |
| `chapters[].title` | yes | Display title. |
| `chapters[].dir` | yes | Folder under `cursus/`. |
| `chapters[].sources` | no | Files of `sources/` this chapter is built from. |
| `sessions[].id` | yes | Slug, unique in the chapter. |
| `sessions[].title` | yes | Display title. The session file has no `#` title of its own. |
| `sessions[].kind` | no | `lesson` (default), `review` or `exam`. Reviews and exams are **timed** (§5.3). |
| `sessions[].duration` | yes | For a lesson, estimated minutes of work at a normal pace, 15 to 120: an estimate of the content, not a time limit, since a lesson ends with its closing. For a review (5 to 120) or an exam (5 to 240), the time allowed. |
| `sessions[].goal` | yes | One sentence: what the learner can do at the end. |
| `sessions[].file` | yes | File in the chapter folder. It may not exist yet while the course is being written. |

The full ID of a session is `<subject>.<chapter>.<session>`, for example
`reseaux.couche-reseau.s11`. The order in the file is the order of the course.

---

## 4. `cursus/lexique.yaml`

Optional: a subject may have no lexicon yet.

```yaml
- id: mac
  term: Adresse MAC
  aliases: [MAC, adresse physique]
  definition: >
    L'adresse « physique » d'une interface réseau (48 bits), **plate** et valable seulement
    sur le lien local.
  see: [arp]
```

| Field | Required | Meaning |
|---|---|---|
| `id` | yes | Slug, unique in the subject. |
| `term` | yes | The term as displayed. |
| `aliases` | no | Other spellings, used for search. |
| `definition` | yes | Markdown, two or three sentences, for a learner who has never seen the term. |
| `see` | no | IDs of related terms. |

---

## 5. Session files

### 5.1 Front matter

```markdown
---
id: s11
objectives:
  - Dire ce que la couche 3 apporte par rapport à la couche 2.
  - Distinguer datagramme et circuit virtuel.
prerequisites: [reseaux.couche-liaison.s05]
terms: [paquet, trame, mac, routeur]
sources:
  - Reseaux_Cours_Intro_2026.pdf#page=14
---
```

| Field | Required | Meaning |
|---|---|---|
| `id` | yes | Must match the session's `id` in the syllabus. |
| `objectives` | yes | One to five sentences starting with a verb. |
| `prerequisites` | no | Full session IDs the learner should have finished before: only those really needed, not simply the previous session. In guided mode the session waits until they are finished; in free mode Boussole advises them. *Resume* goes to a session whose prerequisites are finished. |
| `terms` | no | Lexicon IDs used in the session, shown as its vocabulary list. |
| `sources` | no | Files of `sources/`, optionally with `#page=N`. |
| `written_by` | no (`P012` without it) | Who wrote the session, then who revised or translated it, one line each: a `model` (an AI, by its **exact ID**, such as `claude-opus-5-5`) with the `tool` that ran it, or a `name` (a person); a `date` (`2026-10-21`); a `role`, `wrote` (default), `revised` or `translated`. The learner sees it on the first step of the session. |

A value holding `: ` (colon and space) reads as a key and its value in YAML: put it in quotes,
`- "Lire un diagnostic : du fichier au message."` (`F001` otherwise).

### 5.2 Body

The body is [CommonMark](https://commonmark.org) with:

- GitHub tables and strikethrough;
- math: `$…$` inline, `$$…$$` on its own lines (rendered with KaTeX);
- diagrams: a fenced code block with the language `mermaid` (flowcharts, sequences); for a
  graph of nodes and edges, the `graph` directive (§5.5), which Boussole draws and checks;
- **directives**, following the
  [generic directives proposal](https://talk.commonmark.org/t/generic-directives-plugins-syntax/444)
  (the syntax of `remark-directive`).

The body must not contain a level-1 heading (`#`): the title comes from the syllabus.

### 5.3 Structure of a session

A session is read top to bottom in this order:

1. `:::warmup` — optional, first. Five minutes on earlier sessions of the **same subject**: the
   previous one, and one or two questions from further back, before they are forgotten.
2. **Notions** — each level-2 heading is a notion and carries an ID:
   `## Ce que la couche 3 apporte {#couche3}`. A notion runs until the next level-2 heading or
   section. Notion IDs are unique in their session.
   Inside it: the explanation, **one** `:::example`, then at least one `:::exercise` or `:::mcq`
   (the learner's « À toi »).
3. `:::checkpoint` — optional. A short quiz on the whole session; passing it validates the
   session.
4. `:::practice` — optional, last. Longer final exercises.

Text before the first notion is an introduction. Level-3 headings and below are free inside
a notion.

**Timed sessions** (`kind: review` or `exam`) only ask questions, against the clock: an optional
introduction, then exercises, MCQs and `::ref`s, with no notion and no `example`. Their questions'
estimated times (§5.6) add up to the time allowed. In an exam, hints are not shown and the corrections
come when the time is up or the learner ends the exam; in a review, hints are available before
each answer and the correction right after it.

### 5.4 Directive syntax

| Kind | Syntax | Example |
|---|---|---|
| Container | `:::name[label]{attributes}` … `:::` | `:::example[Ce que vaut la hiérarchie]` |
| Leaf (one line) | `::name[label]{attributes}` | `::video[Neural networks]{youtube=aircAruvnKk start=2:10}` |
| Inline (in a sentence) | `:name[text]{attributes}` | `les :term[adresses MAC]{id=mac} sont plates` |

- Container and leaf directives sit on their own line, starting at its first column. Inside
  fenced code, nothing is a directive.
- Attributes: `{key=value key2="value with spaces" #id}`; `#x` is short for `id=x`; a bare
  `key` means `key=true`. Each attribute is given once.
- A container inside another one uses **more colons on the outside**, one more per level, and
  closes with the same number: `:::hint` (3) inside `::::exercise` (4) inside `:::::warmup` (5).
  A closing line closes the innermost open container that has that many colons.
- Directive names are lowercase English words, whatever the course language.

### 5.5 Content directives

| Directive | Kind | Meaning |
|---|---|---|
| `example` | container | A worked example, solution included and visible. One per notion. |
| `recall` | container | A reminder of a term or prerequisite, just before it is needed. |
| `note`, `tip`, `warning` | container | Callouts. |
| `term` | inline | Hover definition from the lexicon. `{id=…}` is optional when the text equals a term or alias, ignoring case (`:term[Dépôt]` finds the term `dépôt`). |
| `video` | leaf | A video beside the text: `youtube=<11-character video id>` for YouTube, or `src=<file>` for a video file of the sources folder (a lecture recording), never both. `start`, `end` as seconds, `m:ss` or `h:mm:ss`. The label is the caption. A file plays in the browser when it is MP4 (H.264), WebM or Ogg (`F038` otherwise). |
| `pdf` | leaf or inline | Opens a file of `sources/` beside the text. `src=<file>` required; `page=N` optional. |
| `link` | leaf or inline | A web page the course cites (documentation, an article, a reference), opened beside the text: `url=<https://…>` required, a whole web address. Boussole shows the page in place when its site allows it, and otherwise opens it in a window of its own, beside Boussole. The label is the caption, or the words of the sentence. |
| `graph` | container | A small graph that Boussole draws and checks itself (below). The label is the caption; `layout=circle` (default), `row` or `layers`. |

Images use plain Markdown (`![caption](../assets/route.png)`).

#### The `graph` directive

A graph of a few nodes and edges, written as text, so that an agent writes it, Boussole checks it
and draws it the same way in light and dark themes, without any image file. Use it for a
network, a shortest path, a flow, a tree, a state machine; a flowchart or a sequence stays in
`mermaid`, a photo or a hand drawing in an image.

```
:::graph[R2 is down: packets go through R3]{layout=layers}
A "Host A" !
A -> R1 !
R1 -- R2 : down
R1 -> R3 : 2 !
R3 -> R4 : 3 !
:::
```

One line per node or edge, blank lines ignored:

| Line | Meaning |
|---|---|
| `a -> b` | an arrow from `a` to `b` |
| `a -- b` | an edge without direction |
| `… : label` | after an edge: its label, such as a weight, a cost or a capacity (`3/5`) |
| `a "Label"` | a node shown with a label instead of its ID |
| `a @ x,y` | a node at column `x`, row `y` of a grid (whole numbers, from the top left) |
| `… !` | at the end of any line: this node or edge stands out (a path, an answer) |

- A node ID is letters, digits, `_` or `'` (`s`, `v1`, `R2`, `x'`). A node used by an edge needs
  no line of its own; a node line may also stand alone (an isolated node).
- Without `@`, Boussole places the nodes: `circle` in the order they first appear, from the top;
  `row` from left to right; `layers` in columns by distance from the first node, following the
  arrows. With `@`, every node gives its place, and no two share one.
- Labels are plain text: no `$…$`; write `x₁`, `≤`, `∞` with Unicode.
- No edge from a node to itself (a loop is not drawn). Above 12 nodes a graph no longer reads at
  a glance (`P011`): split it, or use an image.

Where each directive may appear:

| Directive | Allowed in |
|---|---|
| `warmup`, `checkpoint`, `practice` | the top level of a session only |
| `exercise`, `mcq` | a notion, an introduction, a section |
| `example` | a notion, an introduction, a section |
| `recall` | the same, and `hint`, `solution` |
| `note`, `tip`, `warning` | the same as `recall`, and `example`, `recall` |
| `video`, `pdf`, `link`, `graph` | anywhere except `answer` |
| `ref` | a notion, an introduction, a section, the body of a review or exam |
| `answer` | `exercise` |
| `hint`, `solution` | `exercise`, `mcq` |

### 5.6 Exercises

Every exercise has an `id` that is **unique in the subject and never reused**. An exercise or a
MCQ may also have `minutes=N`, the time it takes at a normal pace when it differs from the
estimate of its type:

| Question | Estimated minutes |
|---|---|
| `mcq` | 1, or 1.5 with `multiple=true` |
| `order`, `match` | 2 |
| `numeric`, `text` | 3 |
| `fill` | 1 per typed gap, 0.5 per choice gap (at least 1) |
| `free` | 8 |

These estimates size timed sessions. The convention is
`<session>-<letter><n>`: `s11-a1` (À toi), `s11-w1` (warmup), `s11-c1` (checkpoint),
`s11-p1` (practice), and `r19-q1` for a question written in a review or an exam (`q`, since
those have no parts). The validator checks the syntax of an ID (`F004`) and that it is unique
(`F003`), not this convention. Renaming an ID loses the progress recorded on it.

An exercise is a container. Its first part is the statement (Markdown); then come its parts:

| Part | Count | Meaning |
|---|---|---|
| `:::answer` | exactly one, except `free` | What is accepted. Syntax depends on the type, below. |
| `:::hint` | one or more (optional in an exam) | Hints, revealed one at a time in file order, before the learner answers. An exam never shows them: a question written in an exam may have hints, which show when a lesson or a review asks it again with `::ref`. |
| `:::solution` | exactly one | Why the right answer is right: shown beside the question as soon as the learner has answered, with the right answer in green and a wrong one in red. |

```markdown
::::exercise{#s11-a1 type=numeric}
Combien de bits fait une adresse MAC ?

:::answer
48
:::

:::hint
Une adresse MAC s'écrit avec 6 octets, comme `3c:52:82:1a:9f:04`.
:::

:::solution
48 bits (6 octets).
:::
::::
```

#### Exercise types

| `type` | `answer` content | Checking |
|---|---|---|
| `numeric` | One value per line. Decimal point, or a fraction `3/4`. | Attribute `tolerance` (absolute, default `0`). Attribute `unit` is displayed, not typed. The learner may use a comma. |
| `text` | One accepted answer per line. A line starting with `re:` is a regular expression. | Trimmed, spaces collapsed, case ignored. `strict=true` keeps case. |
| `fill` | One line per gap, `N: …`, where the statement has `[[N]]`. A gap is **typed text** (`1: answer \| other accepted answer`, alternatives separated by a `\|` between spaces), a **number** (`2: number 62` or `2: number 3/4 ±0.01`), or a **choice** in a drop-down list (`3: choice *right \| wrong \| other`, the right option starred). A gap may sit in code too, inline or in a block, to complete a command or a line of a program (`` `ip [[1]] show` ``). Outside the statement of a `fill` exercise, `[[N]]` is shown as written. | Typed gaps like `text` and `numeric`; a choice gap by the option picked. The options of a choice gap are shown shuffled, so their order in the file does not matter. |
| `order` | A list (`1. item` or `- item`), in the right order, at least two items. | Items are shown shuffled. |
| `match` | One line per pair: `left => right`, at least two pairs. | Right-hand sides are shown shuffled. |
| `free` | No `answer` part. | The learner writes, reads the solution, then grades themself: *got it / partly / not yet*. For what cannot be checked otherwise. |

Typing must stay easy in a browser: an expected text or number is **short and precise** (a
value, a keyword, an address), never a formula to write in LaTeX. A proof, an algorithm or an
equation is asked as a `fill` whose gaps are mostly **choices**: the learner completes the
reasoning by picking each missing step.

The options of a choice gap and the right-hand sides of a `match` are shown in drop-down lists,
which cannot show maths: write them in plain text, with Unicode symbols where needed (`≤`, `x₁`,
`u²`), never `$…$`. Statements, MCQ choices, `order` items and left-hand sides of a `match` may
hold maths.

#### Multiple choice

A `mcq` is an exercise whose choices are a task list, at the end of its statement. A blockquote indented under a choice is
the feedback shown when the learner picks it: **every wrong choice has one**, saying why it is
wrong. `multiple=true` means several choices are right and the interface says so. The choices
are shown shuffled, the same way each time: their order in the file does not matter.

```markdown
::::mcq{#s11-a2}
Dans quel modèle les routeurs gardent-ils un état **par communication** ?

- [ ] Datagramme
  > Non : en datagramme, chaque paquet porte l'adresse complète et les routeurs ne gardent rien.
- [x] Circuit virtuel
- [ ] Les deux

:::solution
Le circuit virtuel : chaque routeur traversé garde une entrée par circuit établi.
:::
::::
```

A `mcq` has `:::hint` parts like any exercise. It has no `:::answer`: the ticked boxes are the answer.

The learner answers once: the interface then shows the right answer in green, a wrong one in red
with its feedback, and the solution beside the question. Asking again is the job of a `::ref` in
a later session.

#### Reusing an exercise

`::ref{id=s10-a3}` shows an exercise defined elsewhere in the subject. This is how a warmup asks
again a question of an earlier session, and how a review asks again the questions of a
chapter.

---

## 6. Validation

`boussole validate <subject folder>` checks everything below and reports
`file:line: code message`. Errors block the import; warnings do not.

| Code | Level | Rule |
|---|---|---|
| `F001` | error | A YAML file or front matter is missing, unreadable, or lacks a required field or has an invalid one (`subject.yaml`, `syllabus.yaml`, `lexique.yaml`, `learner.yaml`, a session's front matter). |
| `F002` | error | `format` is not a version this Boussole knows. |
| `F003` | error | Duplicate ID (chapter, session, notion, exercise, term). |
| `F004` | error | ID with characters other than `a-z`, `0-9`, `-`. |
| `F010` | error | A session file's `id` does not match the syllabus. |
| `F011` | warning | A session listed in an approved syllabus has no file yet. |
| `F012` | error | A level-1 heading in a session body. |
| `F013` | error | A level-2 heading without an ID. |
| `F020` | error | Unknown directive, a directive of the wrong kind (`:::video`), or in a place it is not allowed (§5.5). |
| `F021` | error | Exercise or mcq without exactly one `solution`; exercise with the wrong number of `answer` parts for its type; an `answer` that is empty or whose lines do not follow its type. |
| `F022` | error | `mcq` with fewer than two choices, no ticked choice, several without `multiple=true`, or text after its choices. |
| `F023` | error | `fill` gaps in the statement and in the answer do not match. |
| `F024` | error | `numeric` answer that is not a number. |
| `F025` | error | `text` or `fill` answer with an invalid regular expression. |
| `F026` | error | A container never closed, or a closing line with no open container of as many colons. |
| `F027` | error | An attribute unknown to its directive or exercise type, a required one missing, or a value that cannot be used. |
| `F030` | error | `ref`, `term`, `prerequisites` or `see` pointing to an ID that does not exist. |
| `F031` | error | `pdf`, `video` with `src` or `sources` naming a file absent from `sources/`, or a page past the end. |
| `F033` | warning | A document of the sources folder that `cursus/sources.yaml` does not list, or lists as `pending`; or documents and no `cursus/sources.yaml` at all (§8). |
| `F034` | warning | A document whose SHA-256 differs from the one `cursus/sources.yaml` recorded: it changed since the course used it. |
| `F035` | warning | A document `cursus/sources.yaml` lists that is no longer in the sources folder. |
| `F036` | warning | The same document (same SHA-256) in several places of the sources folder, more than one not `ignored`. |
| `F037` | error | A `graph` line that is neither a node nor an edge, a node given twice, a loop, places given for some nodes only, or two nodes at the same place. |
| `F038` | warning | A `video` file that browsers may not play (not `.mp4`, `.m4v`, `.webm` or `.ogv`): convert it, for example with `ffmpeg`. |
| `F039` | warning | An entry of `cursus/sources.yaml` with a `url` but no `licence`. |
| `F040` | error | Two versions of a track (same `id`, other `lang`) differ: sessions, notion or question IDs, or what a question asks (§9); or two versions in the same language. |
| `F041` | warning | A session written in another version of the track, not yet in this one (§9). |
| `F042` | error | A `::ref` to a question of the same session or of a later one, in the order of the syllabus: a warmup or a review asks only about what was studied before. |
| `F032` | warning | `video` that YouTube does not know (checked only with `--check-videos`). |
| `P001` | warning | A notion without exercise or `mcq`. |
| `P002` | warning | A notion with more than one `example`. |
| `P003` | warning | `warmup` not first, or `practice` not last. |
| `P004` | warning | Exercise or `mcq` without any `hint`. |
| `P005` | warning | `term` used in the body but not listed in the front matter's `terms`. |
| `P006` | warning | With a `learner.yaml`: a session without the checkpoint, or without the final practice holding an exercise, that `closing` asks for. |
| `P007` | warning | With `quick_check: true`: a notion without a `mcq` before its example. |
| `P008` | warning | A review or exam whose questions' estimated times add up to less than 80 % or more than 110 % of its duration. |
| `P009` | warning | A review or exam holding a notion or an `example`. |
| `P010` | warning | Wrong choice of a `mcq` without feedback. |
| `P011` | warning | A `graph` of more than 12 nodes. |
| `P012` | warning | A session without `written_by`: the learner cannot see who wrote it. |

`P001`, `P002`, `P006` and `P007` apply to lessons only; `P004` does not apply to exams, which
show no hints.

`F` codes are about the format, `P` codes about the teaching contract.

---

## 7. `learner.yaml`

Optional. How the learner learns best: agents shape the sessions after it, and `P006`, `P007`
check that they do. Without it, the defaults below apply and those checks are skipped.

```yaml
background: >
  M1 computer science. Has done linear algebra, never studied duality.
goal: top
session_minutes: 60
explanations: concise
order: example-first
quick_check: true
closing: both
videos: some
notes: >
  Retains better with a quick quiz after each point.
```

| Field | Values | Default | Meaning |
|---|---|---|---|
| `background` | text | empty | What the learner has studied, and what feels shaky. |
| `goal` | `pass`, `good`, `top`, `understand` | `good` | How much practice, and how close to the exam. |
| `session_minutes` | 15 to 120 | `60` | Usual size of a session when planning, timed at a normal pace. |
| `explanations` | `concise`, `detailed` | `concise` | How much an explanation spells out. |
| `order` | `example-first`, `try-first` | `example-first` | Whether a notion's exercise comes after its example, or before. |
| `quick_check` | `true`, `false` | `false` | A short `mcq` right after each notion's explanation, before its example. |
| `closing` | `quiz`, `exercise`, `both` | `quiz` | What ends a session: a `checkpoint`, a final `practice` holding an exercise, or both. |
| `videos` | `none`, `some`, `many` | `some` | How many videos to add. |
| `notes` | text | empty | Anything else, in the learner's words. |

Any other field is an error (`F001`), to catch misspellings.

---

## 8. `cursus/sources.yaml`

Documents arrive during the year: the slides of the next lecture, a new tutorial sheet, a
corrected version, a past exam. `cursus/sources.yaml` records what the course did with each
document of the sources folder, so that Boussole can say what is new and what changed
(`F033`–`F035`), instead of relying on anyone's memory.

```yaml
- file: oc08-s06-Dualite.pdf
  sha256: 9c1e…            # of the document as the course used it
  status: used
  chapters: [dualite]
  note: Transparents du cours 6.
- file: "Annales/**"
  status: pending
  note: Pour l'examen blanc, quand les chapitres seront écrits.
- file: Planning_TD.pdf
  status: ignored
  note: Administratif.
- file: web/tutoriel-python.md     # notes of the agent on a web page
  url: https://docs.python.org/fr/3/tutorial/
  title: Le tutoriel Python
  licence: PSF License
  retrieved: 2026-10-21
  chapters: [bases]
```

| Field | Required | Meaning |
|---|---|---|
| `file` | yes | A document, relative to the sources folder, or a glob pattern (`Annales/**`, `TD*.pdf`) for several. |
| `sha256` | no | The SHA-256 of the document when the course used it. Without it, a change goes unnoticed. Not for a pattern. |
| `status` | no | `used` (default): taken into the course. `ignored`: deliberately left out. `pending`: seen, to be taken in later. |
| `chapters` | no | IDs of the chapters that draw on it. |
| `note` | no | What it is, or why it is ignored or pending. |
| `url` | no | The web page the document was taken from, or that it sums up: a whole address, `https://…`. |
| `title` | no | The title of that page or work. |
| `licence` | with `url` | The licence it is shared under, as its page says: `CC BY-SA 4.0`, `public domain`, `all rights reserved`… (`F039` without it). |
| `retrieved` | no | The day it was retrieved, `2026-10-21`. |

A track built without documents of the learner (the `cursus-discover` playbook) keeps its
sources in the sources folder as well: a document whose licence allows a copy is downloaded
there; any other page gets a **source note**, `web/<name>.md`, the agent's own summary of it
with its address and short quotes, never a copy. Each one has its entry here, with `url` and
`licence`, and the sessions cite them with `:pdf[…]{src=…}` or `:link[…]{url=…}`.

A document is any file of the sources folder, sub-folders included, with one of these extensions: `pdf`, `ppt`, `pptx`,
`odp`, `doc`, `docx`, `odt`, `md`, `txt`, `tex`, `zip`, `png`, `jpg`, `jpeg`, and the videos `mp4`, `m4v`,
`webm`, `ogv`, `mov`, `mkv`, `avi`; hidden files and
folders are not. When the sources folder is the subject folder itself (`sources: .`), the files of
the course (`cursus/`, `subject.yaml`, `learner.yaml`, `AGENTS.md`, `CLAUDE.md`) are not documents.
An entry naming the file exactly wins over a pattern. `boussole sources <subject folder>` lists
what is new, changed, pending or gone, with the entries to add and their SHA-256.

## 9. A track in several languages

A track can exist in several languages: one subject folder per language, each with its own
`lang`, and **the same `id`**. Boussole shows them as one track: the learner picks the language
on the track's page, or switches during a session, and finds the same place in the other
language. Progress is kept by ID, so it is the same in every language.

```
python/
├── en/   subject.yaml: id: python, lang: en
└── fr/   subject.yaml: id: python, lang: fr
```

**Only the text is translated.** Every ID stays the same in every version: sessions, notions
(`{#…}`), exercises and MCQs, so that progress and the place in a session carry over. Each
question asks the same thing: the same kind, the same number of choices with the same ones
right, the same gaps, the same number of items to order or pairs to match. The accepted answers
of a typed gap or a `text` exercise are in the language of their version.

`boussole validate` compares a version with the others it finds: the folders next to it, and
the subjects of `config.yaml` with the same `id`. A difference is an error (`F040`); a session
written in one version and not yet in another is a warning (`F041`), the time to translate it.

To add a language: copy a version's folder, translate its text, set `lang`, and open it with
`boussole open`. The `track-translate` playbook does it with an agent.

## 10. Versions

`format: 1` is this document. A change that makes existing courses invalid increments the
version; Boussole keeps reading older versions, or tells exactly what to change.

A complete subject following this format is in [`examples/reseaux`](../examples/reseaux).
