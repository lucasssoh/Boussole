---
name: seance-write
description: Write one session of a Boussole course from the approved syllabus and the learner's documents, following the course format and its teaching contract (warmup, one worked example per notion, then an exercise right away). Use for each session, in syllabus order.
---

# Write one session

For lessons (`kind: lesson`, the default). Reviews and exams are built with `exam-build`.

Prerequisites: `cursus/syllabus.yaml` has `status: approved`. Write the sessions **in the order of
the syllabus**: each warmup asks about the previous session of the same subject.

Read first: `learner.yaml` (how this learner learns: it shapes everything below),
`.boussole/FORMAT.md` (the format), the syllabus entry of the session (title, duration, goal,
file), the pages of the sources it draws on, `cursus/lexique.yaml`, and the previous session's
file. If `learner.yaml` is missing, follow the `learner-profile` skill first.

## The teaching contract

The learner understands by **doing**, not by reading. Every session follows this shape, adjusted
by `learner.yaml` as the table after it says:

1. **Warmup** (`:::::warmup`, 5 minutes, first): two or three quick questions on the
   **previous session of the same subject** — the learner may have studied something else in
   between. Reuse earlier exercises with `::ref{id=…}` or write new short ones. The first
   session of a subject has no warmup.
2. **Notions**, each a level-2 heading with an ID: `## Le masque de sous-réseau {#masque}`.
   For each notion, in this order:
   1. `:::recall` of the terms and prerequisites the notion needs, **just before** it needs
      them, for a learner who may have forgotten or never seen them.
   2. The explanation: concrete, grounded in the sources. Introduce each term of the
      lexicon on first use with `:term[…]{id=…}`. Point to the source page with
      `::pdf[…]{src=… page=…}`.
   3. **One** `:::example`, fully worked, step by step. One example per notion, never more:
      further cases become exercises.
   4. **Right away**, one or two exercises or MCQs of the **same kind** as the example
      (« À toi »). Every question, quick checks included, has hints, feedback on each wrong
      choice and a solution that explains why the right answer is right (`exercise-author`).
3. **Closing**, as `closing` asks: a **checkpoint** (`:::::checkpoint`), three to five questions
   on the whole session, passing it means the goal is reached; and/or a **practice**
   (`:::::practice`, last), exercises closer to the exam, often taken from the tutorial sheets
   or past exams of the sources (say so in the statement).

How `learner.yaml` changes this shape (without the file, use the defaults):

| Field | Value | The session |
|---|---|---|
| `quick_check` | `true` | right after each explanation, **before** the example, a short `mcq` (one or two questions, 1 minute) on what was just explained |
| `order` | `try-first` | after the explanation (and the quick check), the exercise comes **before** the example; the example then shows the method, so the learner compares |
| `closing` | `quiz` *(default)* | end with a `:::::checkpoint` |
| | `exercise` | end with a `:::::practice` holding at least one `exercise`, the last part of the session |
| | `both` | a checkpoint, then a practice with at least one exercise |
| `explanations` | `concise` / `detailed` | a few sentences per idea / every step spelled out, with an intuition before the formal statement |
| `goal` | `top`, `understand` | more practice, closer to exam level or deeper; `pass`: the essentials, exam-style |
| `background` | text | what to recall or explain from scratch, and what can be taken as known |
| `videos` | `none` | no video; `many`: one per notion where a good one exists |

Size the content to the planned `duration`, timed at a normal pace: about 15 minutes per notion,
5 for the warmup and 10 for the closing. It is an estimate, not a limit: **never cut a notion or
the closing to fit a time**; if the content clearly exceeds the duration, tell the learner the
session should be split, and propose how.

## Writing rules

- **Language**: the `lang` of `subject.yaml`, everywhere in the content. Directive names stay in
  English (`:::example`, not `:::exemple`).
- **Faithful to the sources**: same notations, same vocabulary, same conventions as the
  teacher. If the documents and your knowledge disagree, follow the documents and add a
  `:::note` explaining the difference.
- **No hidden prerequisites**: anything the documents take for granted gets a `:::recall` or a
  short explanation.
- **Draw when a picture helps**: a network, a path, a flow, a tree or a state machine is a
  `:::graph` (`docs/FORMAT.md` §5.5): nodes and edges as lines of text, `!` on what matters,
  checked by `boussole validate`. A flowchart or a sequence is a `mermaid` block. Keep each to a
  few nodes; never invent a figure the sources do not support.
- **Answers are never in the text**: the solution of an exercise is only in its `:::solution`.
  An `:::example` is the only place where a worked answer is visible.
- Front matter: `objectives` (the goal, possibly split in two or three), `terms` (every lexicon
  ID used in the session), `prerequisites` (full session IDs), `sources` (files and pages).
- Exercise IDs: `<session>-w1` (warmup), `<session>-a1` (À toi), `<session>-c1` (checkpoint),
  `<session>-p1` (practice).

For the exercises themselves, follow the `exercise-author` skill. For terms missing from the
lexicon, the `lexique-build` skill. Videos are optional: the `video-find` skill.

## Finish

1. Run `boussole validate .` and fix every error and warning of your session (`cursus-fix`).
2. Tell the learner what the session covers, its exercise count, and anything in the sources
   you were unsure how to explain. When several sessions are to be written, ask whether to go
   on with the next one (interactive tool if you have one: *Next session*, *Stop here*).
