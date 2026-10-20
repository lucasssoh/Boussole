---
name: seance-write
description: Write one session of a Boussole course from the approved syllabus and the learner's documents, following the course format and its teaching contract (warmup, one worked example per notion, then an exercise right away). Use for each session, in syllabus order.
---

# Write one session

Prerequisites: `cursus/syllabus.yaml` has `status: approved`. Write the sessions **in the order of
the syllabus**: each warmup asks about the previous session of the same subject.

Read first: `.boussole/FORMAT.md` (the format), the syllabus entry of the session (title,
duration, goal, file), the pages of the sources it draws on, `cursus/lexique.yaml`, and the
previous session's file.

## The teaching contract

The learner understands by **doing**, not by reading. Every session follows this shape:

1. **Warmup** (`:::::warmup`, 5 minutes, first): two or three quick questions on the
   **previous session of the same subject** — the learner may have studied something else in
   between. Reuse earlier exercises with `::ref{id=…}` or write new short ones. The first
   session of a subject has no warmup.
2. **Notions**, each a level-2 heading with an ID: `## Le masque de sous-réseau {#masque}`.
   For each notion, in this order:
   1. `:::recall` of the terms and prerequisites the notion needs, **just before** it needs
      them, for a learner who may have forgotten or never seen them.
   2. The explanation: short, concrete, grounded in the sources. Introduce each term of the
      lexicon on first use with `:term[…]{id=…}`. Point to the source page with
      `::pdf[…]{src=… page=…}`.
   3. **One** `:::example`, fully worked, step by step. One example per notion, never more:
      further cases become exercises.
   4. **Right away**, one or two exercises or qcms of the **same kind** as the example
      (« À toi »), with hints.
3. **Checkpoint** (`:::::checkpoint`): three to five questions on the whole session. Passing it
   means the session's goal is reached.
4. **Practice** (`:::::practice`, last, optional): longer exercises, closer to the exam, often
   taken from the tutorial sheets or past exams of the sources (say so in the statement).

Keep the session within its planned duration: count about 15 minutes per notion, 5 for the
warmup and 10 for the checkpoint.

## Writing rules

- **Language**: the `lang` of `subject.yaml`, everywhere in the content. Directive names stay in
  English (`:::example`, not `:::exemple`).
- **Faithful to the sources**: same notations, same vocabulary, same conventions as the
  teacher. If the documents and your knowledge disagree, follow the documents and add a
  `:::note` explaining the difference.
- **No hidden prerequisites**: anything the documents take for granted gets a `:::recall` or a
  short explanation.
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
   you were unsure how to explain.
