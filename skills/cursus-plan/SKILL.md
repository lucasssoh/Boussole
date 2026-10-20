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
   itself). Never modify, move or rename anything in it.
3. List every document there. For each one, note its kind (lecture notes, slides, tutorial
   sheet, lab, past exam, project brief) and its number of pages.
4. Read them all. If you cannot read a PDF directly, extract its text with
   `pdftotext -layout <file> -`. Read the **past exams** and **tutorial sheets** with special
   care: they show what the learner will be asked to do.
5. Check `lang` in `subject.yaml`: it must be the language of the documents (BCP 47: `fr`,
   `en`…). Correct it if it is not.

## 2. Cut the course

- **Chapters follow the teacher's order**, not your own idea of the topic. One chapter is
  usually one part of the lecture notes.
- **A session lasts about `session_minutes`** of `learner.yaml` (60 by default) of active work
  for someone who discovers the topic: reading, one worked example per notion, then exercises.
  Count about 15 minutes per notion. When in doubt, cut smaller.
- **Each session has one goal**, a sentence starting with a verb, that the learner can check:
  "Compute the network address of a host from its mask", not "Understand IPv4".
- **Do not assume earlier courses are known.** If a document relies on a notion the learner may
  never have studied (a proof technique, a probability law, a programming idiom), plan a
  session for it before the first one that needs it, in the chapter where it is first needed.
- **Tutorial sheets and past exams are not chapters.** Their exercises go into the sessions of
  the notions they train; a final chapter may gather exam-style practice.
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
- Leave `status: proposed`. **Only the learner sets `approved`.**

Then run `boussole validate .` and fix every error (see the `cursus-fix` skill).

## 4. Stop and present

Show the learner a table: chapter, session, duration, goal, and the pages of the sources each
session draws on. Give the total number of sessions and hours. Point out what you were unsure
of: notions you added because a prerequisite seemed missing, parts of the documents you left
out and why.

Ask the learner to read it, change it if they wish, and set `status: approved`. Do not write any
session before that.
