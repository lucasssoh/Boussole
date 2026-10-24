## Boussole course

This folder is a Boussole subject. The learner's documents are in {{sources}}. The course
built from them is in `cursus/`, in the format described in `.boussole/FORMAT.md`.

To build or change the course, follow the skills in `.boussole/skills/`. **Read a skill's
`SKILL.md` before doing its task.**

| Task | Skill |
|---|---|
| Ask how the learner learns (`learner.yaml`) | `.boussole/skills/learner-profile/SKILL.md` |
| Find the sources when there are no documents (a track started with `boussole new`) | `.boussole/skills/cursus-discover/SKILL.md` |
| Propose or revise the plan of the course | `.boussole/skills/cursus-plan/SKILL.md` |
| Write a session | `.boussole/skills/seance-write/SKILL.md` |
| Write or change an exercise or an MCQ | `.boussole/skills/exercise-author/SKILL.md` |
| Build a review or a mock exam (`kind: review` or `exam`) | `.boussole/skills/exam-build/SKILL.md` |
| Add terms to the lexicon | `.boussole/skills/lexique-build/SKILL.md` |
| Add videos | `.boussole/skills/video-find/SKILL.md` |
| Take in documents that arrived or changed | `.boussole/skills/sources-update/SKILL.md` |
| Write the course in another language (a version of the track) | `.boussole/skills/track-translate/SKILL.md` |
| Fix what the validator reports | `.boussole/skills/cursus-fix/SKILL.md` |

Where to start:

- **First, every time**: run `boussole validate .`. Once the course has a plan (chapters in
  `cursus/syllabus.yaml`), if it reports `F033` to `F036` (a document arrived, changed,
  went, or sits in two places), or the learner says they added documents, follow **sources-update** before anything
  else: the course must follow the documents. Before there is a plan, **cursus-plan** records
  the documents itself.
- No `learner.yaml` → **learner-profile** first, right after the learner's first request; then
  carry on with that request.
- The sources folder holds no document, or the learner wants to learn something without giving
  documents → **cursus-discover**: it finds and cites the sources, then hands over to
  **cursus-plan**.
- `cursus/syllabus.yaml` has no chapter → **cursus-plan**.
- `status: proposed` → the learner has not approved the plan yet: show it and ask for approval
  (**cursus-plan**, §4).
- `status: approved` → write the first session of the syllabus that has no file yet, one at a
  time, in order: **seance-write** for a lesson, **exam-build** for a review or an exam.

Writing or revising many sessions (a whole track, several chapters):

- **One agent per chapter**, never one agent for the whole track: an agent that carries the
  whole track re-reads it at every step, which multiplies the cost. Three at a time at most,
  so that a usage limit does not stop them all half-way.
- **Each agent writes only in its chapter's folder** (`cursus/<chapter>/`). Everything else is
  read-only for it: the other chapters, `syllabus.yaml`, `lexique.yaml`, `sources.yaml`,
  `learner.yaml`. It never renames or deletes a session or exercise ID, which other chapters
  `::ref`. A change to a shared file (a new term, a change of the plan) goes in its report, and
  the agent that coordinates makes it, one at a time.
- Each agent checks with `boussole validate .` and fixes what concerns its chapter; the
  coordinator runs it on the whole track at the end.
- `boussole index .` lists every question of the track: an agent picks its warmup and review
  questions there rather than opening every session.

Which model, when you can choose (in Claude Code, the `model` of an agent):

| Task | Model |
|---|---|
| The plan; writing a lesson, its examples and the wrong choices of its MCQs | your most capable |
| A review or warmup made of `::ref`, prerequisites, fixing what the validator reports, a translation | a mid-sized one |
| Listing, checking the form | a fast one |

When in doubt, the most capable: a wrong course costs more than the tokens it saved.

Rules:

- **Never modify, move or delete the learner's documents**, in {{sources}}.
- **Asking the learner**: when you need a choice from them, use your interactive
  multiple-choice tool if you have one (Claude Code: `AskUserQuestion`, answered with the arrow
  keys), with the recommended option first; otherwise ask in a message with numbered choices.
- Write only `subject.yaml`, `learner.yaml` and files under `cursus/`; and, for a track without
  documents (**cursus-discover**), the sources you download or the source notes you write in
  {{sources}}, never anything else there.
- **Every session you write, revise or translate says so** in its `written_by`: your exact model
  ID (the one you run as), your tool, today's date, and `role`. Never remove another author's line.
- Every session follows `learner.yaml`: how notions are ordered, the quick checks, how sessions
  close.
- The course is written in the language given by `lang` in `subject.yaml`.
- In the skills, `boussole validate .` means, from this folder:

  ```
  {{validate}}
  ```

  Run it after every change, until it ends without error, before saying a task is done.
  `boussole sources .` and `boussole index .` are the same command with `sources` or `index` in
  place of `validate`.
