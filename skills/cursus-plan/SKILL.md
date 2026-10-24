---
name: cursus-plan
description: Read a learner's course documents and propose the syllabus of a Boussole course (chapters and sessions), then stop for the learner's approval. Use first, before any session is written, or when the documents change.
---

# Plan a course from the learner's documents

You turn the documents in the subject's sources folder into `cursus/syllabus.yaml`. You do **not**
write sessions here: the learner approves the plan first.

## 1. Know the subject and the learner

1. If there is no `learner.yaml`, follow the `learner-profile` skill first. Its
   `session_minutes` sets the usual length of a session, its `background` tells which
   prerequisites to plan for, and its `goal` how much exam practice to plan.
2. Read `subject.yaml`. `sources` names the folder of documents (`.` means the subject folder
   itself). Never modify, move or rename anything in it. If it holds no document, follow
   `cursus-discover` first: it finds the sources, then comes back here.
3. List every document there. For each one, note its kind (lecture notes, slides, tutorial
   sheet, lab, past exam, project brief) and its number of pages.
4. Read them all. If you cannot read a PDF directly, extract its text with
   `pdftotext -layout <file> -`. Read the **past exams** and **tutorial sheets** with special
   care: they show what the learner will be asked to do.
5. Check `lang` in `subject.yaml`: it must be the language of the documents (BCP 47: `fr`,
   `en`…). Correct it if it is not.
6. Set `level` and `domain` in `subject.yaml` (`.boussole/FORMAT.md` §2). `level` is what it
   takes to follow the track **without too much trouble**, judged from the documents and what
   they take for granted, not from the learner: `discovery` (nothing), `secondary`, `bachelor`,
   `master` or `research`. A first-year master course is `master`, even for a learner who finds it
   easy. `domain` is its field, from the list of §2 only. Both show in the plan you present (§4),
   so that the learner can question them before approving.

## 2. Cut the course

- **Chapters follow the teacher's order**, not your own idea of the topic. One chapter is
  usually one part of the lecture notes.
- **A session holds about `session_minutes`** of `learner.yaml` (60 by default) of active work,
  **timed at a normal pace** for someone who discovers the topic: reading, one worked example per
  notion, then exercises. Count about 15 minutes per notion. It is an estimate of content, not a
  time limit: the learner goes faster or slower depending on the day, and the session ends with
  its closing. When in doubt, cut smaller.
- **Each session has one goal**, a sentence starting with a verb, that the learner can check:
  "Compute the network address of a host from its mask", not "Understand IPv4".
- **Do not assume earlier courses are known.** If a document relies on a notion the learner may
  never have studied (a proof technique, a probability law, a programming idiom), plan a
  session for it before the first one that needs it, in the chapter where it is first needed.
- **Tutorial sheets and past exams are not chapters.** Their exercises go into the sessions of
  the notions they train.
- **Plan timed sessions too** (`kind`, see `.boussole/FORMAT.md` §3): a **review**
  (`kind: review`, 10 to 20 minutes) at the end of each chapter, and **mock exams**
  (`kind: exam`) before each real exam, as long as the real one (from the past exams, or ask the
  learner). Their `duration` is the time allowed, not an estimate. They are built with the
  `exam-build` skill. Add a **cumulative review** (`kind: review`, 20 to 30 minutes) halfway
  through the track and before each exam, on every chapter so far: what was learnt weeks ago is
  what is forgotten, and asking it again then is what makes it stay.
- **List the notions and what each needs.** For each notion, the earlier notions it uses. A
  notion used before the session that teaches it is a mistake of the plan: move it, or plan its
  session earlier. These dependencies become the `prerequisites` of the sessions: only those
  really needed, since in guided mode a session waits for them. A prerequisite in another
  chapter makes the whole chapter wait for that chapter: order the chapters so that each needs
  only earlier ones (`F044` otherwise), and keep what a chapter needs from another as small as
  the notions allow.
- **Note the usual mistakes** the documents point out (warnings of the teacher, corrections of
  tutorial sheets, comments on past exams): they become the wrong choices of the MCQs.
- A project brief gets its own chapter, cut into sessions by the steps of the project.

## 3. Write the syllabus

Write `cursus/syllabus.yaml` exactly as in `.boussole/FORMAT.md` §3:

```yaml
status: proposed
chapters:
  - id: couche-reseau
    title: La couche réseau
    dir: 01-couche-reseau
    sources: [Reseaux_Cours_Intro_2026.pdf]
    sessions:
      - id: s11
        title: Rôle de la couche réseau
        duration: 60
        goal: Dire ce que la couche 3 apporte par rapport à la couche 2.
        file: 11-role-couche-reseau.md
```

- Titles and goals are in the language of the course.
- IDs: lowercase letters, digits and `-`. Chapter folders `01-…`, `02-…`; session IDs `s11`,
  `s12`… where the tens digit is the chapter's rank, so they stay short and sortable.
- **Never change the ID of a session that already has a file**: the learner's progress is
  stored against it. Add, reorder or retitle instead.
- Leave `status: proposed`. **Only the learner approves**: you set `approved` only after they
  explicitly chose to approve (§4).

Then write `cursus/sources.yaml` (`.boussole/FORMAT.md` §8): one entry per document, `used`
with the chapters that draw on it, `ignored` or `pending` with a note for the others. Take each
`sha256` from `boussole sources .` (same program as `boussole validate .`); never compute it
yourself. From then on, Boussole tells you when a document arrives or changes (`sources-update`).

Then run `boussole validate .` and fix every error (see the `cursus-fix` skill).

## 4. Stop and present

Give the track's `level` and `domain` first, then show the learner a table: chapter, session, estimated duration, goal, the pages of the sources each
session draws on, and the sessions it needs first. Give the total number of sessions and hours. Point out what you were unsure
of: notions you added because a prerequisite seemed missing, parts of the documents you left
out and why.

Then ask for approval. With an interactive question tool (Claude Code: `AskUserQuestion`), ask
"Do you approve this plan?" with the options *Approve*, *Change something*, *Not now*; without
one, ask the same in a message. On *Approve*, set `status: approved` and say so. On *Change
something*, apply their changes, show the plan again and ask again. On *Not now*, stop: they can
also edit `cursus/syllabus.yaml` themselves and set `status: approved`.

Do not write any session before the plan is approved.
