---
name: seance-write
description: Write one session of a Boussole course from the approved syllabus and the learner's documents, following the course format and its teaching contract (warmup, one worked example per notion, then an exercise right away). Use for each session, in syllabus order.
---

# Write one session

For lessons (`kind: lesson`, the default). Reviews and exams are built with `exam-build`.

Prerequisites: `cursus/syllabus.yaml` has `status: approved`. Write the sessions **in the order of
the syllabus**: each warmup asks about earlier sessions of the same subject.

Read first: `learner.yaml` (how this learner learns: it shapes everything below),
`.boussole/FORMAT.md` (the format), the syllabus entry of the session (title, duration, goal,
file), the pages of the sources it draws on, `cursus/lexique.yaml`, and the previous session's
file. If `learner.yaml` is missing, follow the `learner-profile` skill first.

## The teaching contract

The learner understands by **doing**, not by reading. Every session follows this shape, adjusted
by `learner.yaml` as the table after it says:

1. **Warmup** (`:::::warmup`, 5 minutes, first): two or three quick questions on **earlier
   sessions of the same subject**: one on the previous session, and **one or two from two to
   five sessions back**, or from an earlier chapter. Asking only about the previous session
   checks what is still fresh; what was learnt a while ago is what is about to be forgotten, and
   retrieving it then is what makes it stay. Reuse earlier exercises with `::ref{id=…}` or
   write new short ones. Pick them from `boussole index . --before <session>`, which lists every
   earlier question with how many times it is already asked again (`again`): prefer those asked
   again rarely, from different notions, so that the warmups of the track do not keep asking the
   same few questions; never a question of an exam.
   The warmup may also hold **one hook question** on what the session is about to teach,
   especially when it opens a new topic: a guess the learner can make from everyday intuition
   or from what they already know (« À ton avis… », « Devine… »), never one that needs the new
   notion. Trying to answer before the lesson, even wrongly, makes the lesson stick better
   (the pretesting effect). Write it as a new question (never a `::ref` to a later session), say
   in its statement that it is a guess, mark it `hook=true` (it earns no points), and let its
   solution open the session with no blame: nobody is expected to know yet. The
   first session of a subject has no warmup; the second asks only about the first.
2. **Notions**, each a level-2 heading with an ID: `## Le masque de sous-réseau {#masque}`.
   For each notion, in this order:
   1. `:::recall` of the terms and prerequisites the notion needs, **just before** it needs
      them, for a learner who may have forgotten or never seen them.
   2. The explanation: concrete, grounded in the sources. Introduce each term of the
      lexicon on first use with `:term[…]{id=…}`. Point to the source page with
      `::pdf[…]{src=… page=…}`.
   3. **One** `:::example`, fully worked, step by step. One example per notion, never more:
      further cases become exercises. **Fade the help** along a chapter: in its first session
      the examples are complete; later, once the method has been shown, a notion that applies
      it again may follow its short example with a **half-solved** `fill` (the first steps
      given, the last ones to complete) before the learner does it alone.
   4. **Right away**, one or two exercises or MCQs of the **same kind** as the example
      (« À toi »). Every question, quick checks included, has hints, feedback on each wrong
      choice and a solution that explains why the right answer is right (`exercise-author`).
      Among a notion's questions, one **changes the setting** (another context, the question
      asked the other way round), not only the values, so that the learner recognises the
      method rather than the example. For a notion about an idea rather than a calculation, an
      **answer + reason** question (`exercise-author`) shows whether it is understood.
3. **Closing**, as `closing` asks: a **checkpoint** (`:::::checkpoint`), three to five questions
   on the whole session, passing it means the goal is reached; and/or a **practice**
   (`:::::practice`, last), exercises closer to the exam, often taken from the tutorial sheets
   or past exams of the sources (say so in the statement). The closing **mixes** the notions
   of the session rather than taking them one after the other, and asks at least once **which
   method applies** (two close notions, such as two formulas or two algorithms, opposed in one
   question): choosing the method is part of what the exam checks.

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
- **Nothing that only decorates**: an anecdote, an image or a video that does not serve the
  goal takes attention away from it. Leave it out.
- **Go from one form to another**: when the course shows the same thing in several forms (a
  formula, a graph, a table, code), ask at least one question that goes from one to the other.
- **No hidden prerequisites**: anything the documents take for granted gets a `:::recall` or a
  short explanation.
- **Cite what you rely on**: a page of the documents is a `:pdf[…]{src=… page=…}`, a web page a
  `:link[…]{url=…}` in the sentence, or a `::link[…]{url=…}` block for a reference worth
  reading in full. The learner opens it beside the text.
- **Draw when a picture helps**: a network, a path, a flow, a tree or a state machine is a
  `:::graph` (`docs/FORMAT.md` §5.5): nodes and edges as lines of text, `!` on what matters,
  checked by `boussole validate`. A flowchart or a sequence is a `mermaid` block. Keep each to a
  few nodes; never invent a figure the sources do not support.
- **Answers are never in the text**: the solution of an exercise is only in its `:::solution`.
  An `:::example` is the only place where a worked answer is visible.
- **Say who wrote it**: the front matter gets `written_by` with your **exact model ID** (the one
  you run as, such as `claude-opus-5-5`, never a guess or a family name), the `tool` you run in
  and today's `date`. When you revise a session someone else wrote, keep their line and add
  yours with `role: revised`. The learner sees it.
- Front matter: `objectives` (the goal, possibly split in two or three), `terms` (every lexicon
  ID used in the session), `prerequisites` (full session IDs), `sources` (files and pages).
- `prerequisites` lists only the sessions this one **really needs** (a notion it uses without
  explaining it again), never simply the previous one: in guided mode the learner cannot begin
  the session until they are finished, so a needless prerequisite blocks them for nothing.
- Exercise IDs: `<session>-w1` (warmup), `<session>-a1` (À toi), `<session>-c1` (checkpoint),
  `<session>-p1` (practice).

For the exercises themselves, follow the `exercise-author` skill. For terms missing from the
lexicon, the `lexique-build` skill. Videos are optional: the `video-find` skill.

## Finish

1. Run `boussole validate .` and fix every error and warning of your session (`cursus-fix`).
2. Tell the learner what the session covers, its exercise count, and anything in the sources
   you were unsure how to explain. When several sessions are to be written, ask whether to go
   on with the next one (interactive tool if you have one: *Next session*, *Stop here*).
