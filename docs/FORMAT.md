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
| `sessions[].duration` | yes | Estimated minutes of work at a normal pace, 15 to 120. An estimate of the content, not a time limit: a session ends with its closing. |
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
| `prerequisites` | no | Full session IDs the learner should have done before. |
| `terms` | no | Lexicon IDs used in the session, shown as its vocabulary list. |
| `sources` | no | Files of `sources/`, optionally with `#page=N`. |

### 5.2 Body

The body is [CommonMark](https://commonmark.org) with:

- GitHub tables and strikethrough;
- math: `$…$` inline, `$$…$$` on its own lines (rendered with KaTeX);
- diagrams: a fenced code block with the language `mermaid`;
- **directives**, following the
  [generic directives proposal](https://talk.commonmark.org/t/generic-directives-plugins-syntax/444)
  (the syntax of `remark-directive`).

The body must not contain a level-1 heading (`#`): the title comes from the syllabus.

### 5.3 Structure of a session

A session is read top to bottom in this order:

1. `:::warmup` — optional, first. Five minutes on the previous session of the **same subject**.
2. **Notions** — each level-2 heading is a notion and carries an ID:
   `## Ce que la couche 3 apporte {#couche3}`. A notion runs until the next level-2 heading or
   section. Notion IDs are unique in their session.
   Inside it: the explanation, **one** `:::example`, then at least one `:::exercise` or `:::qcm`
   (the learner's « À toi »).
3. `:::checkpoint` — optional. A short quiz on the whole session; passing it validates the
   session.
4. `:::practice` — optional, last. Longer final exercises.

Text before the first notion is an introduction. Level-3 headings and below are free inside
a notion.

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
| `term` | inline | Hover definition from the lexicon. `{id=…}` is optional when the text equals a term or alias. |
| `video` | leaf | YouTube video beside the text. `youtube=<11-character video id>` required; `start`, `end` as seconds, `m:ss` or `h:mm:ss`. The label is the caption. |
| `pdf` | leaf or inline | Opens a file of `sources/` beside the text. `src=<file>` required; `page=N` optional. |

Images use plain Markdown (`![caption](../assets/route.png)`).

Where each directive may appear:

| Directive | Allowed in |
|---|---|
| `warmup`, `checkpoint`, `practice` | the top level of a session only |
| `exercise`, `qcm` | a notion, an introduction, a section |
| `example` | a notion, an introduction, a section |
| `recall` | the same, and `hint`, `solution` |
| `note`, `tip`, `warning` | the same as `recall`, and `example`, `recall` |
| `video`, `pdf` | anywhere except `answer` |
| `ref` | `warmup`, `checkpoint`, `practice` |
| `answer` | `exercise` |
| `hint`, `solution` | `exercise`, `qcm` |

### 5.6 Exercises

Every exercise has an `id` that is **unique in the subject and never reused**. The convention is
`<session>-<letter><n>`: `s11-a1` (À toi), `s11-w1` (warmup), `s11-c1` (checkpoint),
`s11-p1` (practice). Renaming an ID loses the progress recorded on it.

An exercise is a container. Its first part is the statement (Markdown); then come its parts:

| Part | Count | Meaning |
|---|---|---|
| `:::answer` | exactly one, except `free` | What is accepted. Syntax depends on the type, below. |
| `:::hint` | zero or more | Hints, revealed one at a time in file order. |
| `:::solution` | exactly one | The worked solution, shown after the first attempt. |

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
| `fill` | One line per gap: `1: answer \| other accepted answer`, alternatives separated by a `\|` between spaces. The statement marks gaps with `[[1]]`, `[[2]]`… | Each gap checked like `text`. |
| `order` | A list (`1. item` or `- item`), in the right order, at least two items. | Items are shown shuffled. |
| `match` | One line per pair: `left => right`, at least two pairs. | Right-hand sides are shown shuffled. |
| `free` | No `answer` part. | The learner writes, reads the solution, then grades themself: *got it / partly / not yet*. For proofs and models. |

#### Multiple choice

A `qcm` is an exercise whose choices are a task list, at the end of its statement. A blockquote indented under a choice is
the feedback shown when the learner picks it. `multiple=true` means several choices are right
and the interface says so.

```markdown
::::qcm{#s11-a2}
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

A `qcm` may also have `:::hint` parts. It has no `:::answer`: the ticked boxes are the answer.

#### Reusing an exercise

`::ref{id=s10-a3}` inside `warmup`, `checkpoint` or `practice` shows an exercise defined
elsewhere in the subject. This is how a warmup asks again a question of the previous session.

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
| `F021` | error | Exercise or qcm without exactly one `solution`; exercise with the wrong number of `answer` parts for its type; an `answer` that is empty or whose lines do not follow its type. |
| `F022` | error | `qcm` with fewer than two choices, no ticked choice, several without `multiple=true`, or text after its choices. |
| `F023` | error | `fill` gaps in the statement and in the answer do not match. |
| `F024` | error | `numeric` answer that is not a number. |
| `F025` | error | `text` or `fill` answer with an invalid regular expression. |
| `F026` | error | A container never closed, or a closing line with no open container of as many colons. |
| `F027` | error | An attribute unknown to its directive or exercise type, a required one missing, or a value that cannot be used. |
| `F030` | error | `ref`, `term`, `prerequisites` or `see` pointing to an ID that does not exist. |
| `F031` | error | `pdf` or `sources` naming a file absent from `sources/`, or a page past the end. |
| `F032` | warning | `video` that YouTube does not know (checked only with `--check-videos`). |
| `P001` | warning | A notion without exercise or `qcm`. |
| `P002` | warning | A notion with more than one `example`. |
| `P003` | warning | `warmup` not first, or `practice` not last. |
| `P004` | warning | Exercise without any `hint`. |
| `P005` | warning | `term` used in the body but not listed in the front matter's `terms`. |
| `P006` | warning | With a `learner.yaml`: a session without the checkpoint, or without the final practice holding an exercise, that `closing` asks for. |
| `P007` | warning | With `quick_check: true`: a notion without a `qcm` before its example. |

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
| `quick_check` | `true`, `false` | `false` | A short `qcm` right after each notion's explanation, before its example. |
| `closing` | `quiz`, `exercise`, `both` | `quiz` | What ends a session: a `checkpoint`, a final `practice` holding an exercise, or both. |
| `videos` | `none`, `some`, `many` | `some` | How many videos to add. |
| `notes` | text | empty | Anything else, in the learner's words. |

Any other field is an error (`F001`), to catch misspellings.

---

## 8. Versions

`format: 1` is this document. A change that makes existing courses invalid increments the
version; Boussole keeps reading older versions, or tells exactly what to change.

A complete subject following this format is in [`examples/reseaux`](../examples/reseaux).
