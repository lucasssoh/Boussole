## Boussole course

This folder is a Boussole subject. The learner's documents are in {{sources}}. The course
built from them is in `cursus/`, in the format described in `.boussole/FORMAT.md`.

To build or change the course, follow the skills in `.boussole/skills/`. **Read a skill's
`SKILL.md` before doing its task.**

| Task | Skill |
|---|---|
| Ask how the learner learns (`learner.yaml`) | `.boussole/skills/learner-profile/SKILL.md` |
| Propose or revise the plan of the course | `.boussole/skills/cursus-plan/SKILL.md` |
| Write a session | `.boussole/skills/seance-write/SKILL.md` |
| Write or change an exercise or a qcm | `.boussole/skills/exercise-author/SKILL.md` |
| Add terms to the lexicon | `.boussole/skills/lexique-build/SKILL.md` |
| Add videos | `.boussole/skills/video-find/SKILL.md` |
| Fix what the validator reports | `.boussole/skills/cursus-fix/SKILL.md` |

Where to start:

- No `learner.yaml` → **learner-profile** first, right after the learner's first request; then
  carry on with that request.
- `cursus/syllabus.yaml` has no chapter → **cursus-plan**.
- `status: proposed` → the learner has not approved the plan yet: show it to them and wait.
- `status: approved` → write the first session of the syllabus that has no file yet
  (**seance-write**), one at a time, in order.

Rules:

- **Never modify, move or delete the learner's documents**, in {{sources}}.
- Write only `subject.yaml`, `learner.yaml` and files under `cursus/`.
- Every session follows `learner.yaml`: how notions are ordered, the quick checks, how sessions
  close.
- The course is written in the language given by `lang` in `subject.yaml`.
- In the skills, `boussole validate .` means, from this folder:

  ```
  {{validate}}
  ```

  Run it after every change, until it ends without error, before saying a task is done.
