---
name: sources-update
description: Take into a Boussole course the documents that arrived or changed in the sources folder (new slides, a new tutorial sheet, a corrected version, a past exam), without breaking what is written. Use whenever boussole validate reports F033, F034 or F035, or the learner says they added documents.
---

# Take new or changed documents into the course

A learner rarely has all the documents at the start: they arrive lecture after lecture. Boussole
compares the sources folder with `cursus/sources.yaml` and reports the difference
(`.boussole/FORMAT.md` §8). Your job is to bring the course up to date **without breaking it**:
the learner's progress is stored against the IDs, and sessions already studied must stay as they
are unless a document says they are wrong.

## 1. See what changed

Run, from the subject folder:

```
boussole sources .
```

(the same program as `boussole validate .`, with `sources` instead of `validate`; see
`AGENTS.md`). It lists each document that is **new**, **changed**, **pending** or **gone**, and
prints the entries to add to `cursus/sources.yaml`, SHA-256 included. Never compute or invent a
SHA-256 yourself: copy it from there.

The sources folder is searched **with all its sub-folders**: a folder per lecture or per week
is fine. Your own folders (`cursus/`, `.boussole/`) are never documents.

No `cursus/sources.yaml` yet, on a course already planned? Then every document shows as new.
Write the file first (§4) for what the course **already** uses, from the `sources` of the
chapters and sessions; mark `ignored` what it deliberately leaves out (use a pattern for a whole
folder); only what remains is really new.

## 2. Read and classify each new or changed document

Read it (for a PDF, `pdftotext -layout <file> -`). Decide what it is, one document at a time:

| The document is… | Do |
|---|---|
| the next part of the course (a later lecture) | new sessions, in a new chapter or at the end of the right one (§3) |
| material for a chapter already planned (slides, notes) | add it to that chapter's `sources`; write its sessions if they are not written yet |
| a tutorial sheet or exercises | new exercises: the `practice` of the sessions concerned, or a new practice session |
| a past exam or an exam brief | the mock exam (`exam-build`), and the balance of the reviews |
| a corrected version of a document already used (F034) | find what changed (diff the text of the old and new versions if you can, else compare with the sessions); fix only the sessions concerned |
| a duplicate, an administrative paper, something off the subject | `status: ignored`, with a `note` saying why |
| useful later, but not now (next semester, a project) | `status: pending`, with a `note` |
| gone (F035) | if sessions cite it (`sources`, `::pdf`), point them to the document that replaces it, else remove the entry |
| the same document as another one, in another place (F036) | keep the one the teacher's folder structure puts in its place, point sessions to it, mark the copies `ignored` with a note naming the one kept |
| notes or sheets **generated earlier** (by an AI, by you, by another tool), not the teacher's | help, never authority: check them against the teacher's documents, prefer the teacher's wording; `used` with a note saying they are generated, or `ignored` if the course covers them already |

## 3. Change the plan without breaking it

- **Add, never renumber.** New chapters and sessions get **new IDs** (the next free number:
  `s27` after `s26`, a new chapter's tens digit after the last one). Never change the ID of a
  session that has a file, and never reuse one.
- **Do not rewrite a written session wholesale.** Fix what a corrected document changed; add
  exercises to its practice. Its notions and exercise IDs stay.
- Respect the teacher's order: a later lecture goes after what is already there, even if the
  plan would be nicer otherwise.
- Reviews and mock exams follow: a new chapter gets its review (`exam-build`); a mock exam is
  extended or rebuilt only when the new material belongs to the exam.
- Keep the durations honest (`cursus-plan` §2).

If the plan changes (new chapters or sessions, sessions moved), set `status: proposed`, show the
learner **only what changes** (a table: added, changed, unchanged count) and ask for approval as
in `cursus-plan` §4. Adding exercises or fixing a session from a corrected document does not need
approval; say what you did.

## 4. Record it in `cursus/sources.yaml`

For each document handled, write or update its entry: the `sha256` printed by
`boussole sources .`, the `status`, the `chapters` that use it, a short `note`. A changed
document gets its **new** `sha256` once the sessions follow it.

## 5. Check

A document that comes from the web keeps where it comes from in its entry: `url`, `title`,
`licence` and `retrieved` (`docs/FORMAT.md` §8). Never copy a page whose licence does not allow
it: write a source note `web/<name>.md` in your own words instead, with its address and short
quotes.

Run `boussole validate .` until there is no error; `F033`–`F035` must be gone for every document
you handled (`pending` ones stay, by design). Then write the new sessions in order, as usual
(`seance-write`, `exam-build`).

Tell the learner, in a few lines: which documents you took in, what each one changed in the
course, which you ignored or left pending, and why.
