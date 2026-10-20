# Agent skills

Instructions for AI agents that build a course from a learner's documents, in the
[Agent Skills](https://agentskills.io) format: one folder per skill, with a `SKILL.md`.

`boussole init <subject folder>` installs them in `<subject>/.boussole/skills/`, next to a copy of
the [course format](../docs/FORMAT.md) and an `AGENTS.md` that tells any agent where to start.

| Skill | Role |
|---|---|
| [`learner-profile`](learner-profile/SKILL.md) | Asks the learner how they learn best, and writes `learner.yaml`. |
| [`cursus-plan`](cursus-plan/SKILL.md) | Reads the sources and proposes the syllabus, then stops for the learner. |
| [`seance-write`](seance-write/SKILL.md) | Writes one session. |
| [`exercise-author`](exercise-author/SKILL.md) | Writes exercises and MCQs that teach, with answers that are right. |
| [`exam-build`](exam-build/SKILL.md) | Builds reviews and mock exams whose questions fill the time allowed. |
| [`lexique-build`](lexique-build/SKILL.md) | Builds the lexicon. |
| [`video-find`](video-find/SKILL.md) | Adds videos that exist and fit. |
| [`sources-update`](sources-update/SKILL.md) | Takes documents that arrive or change into the course, without breaking what is written. |
| [`cursus-fix`](cursus-fix/SKILL.md) | Runs the validator and fixes until there is no error. |

A skill is written for the agent, in English, whatever the language of the course: the course
itself is written in the language of its sources.
