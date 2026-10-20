---
name: exam-build
description: Build a timed session of a Boussole course — a review or a mock exam — whose number of questions fits the time allowed, mostly multiple-choice, drawn from the lessons and the past exams. Use for every session of kind review or exam in the syllabus.
---

# Build a review or a mock exam

A lesson teaches; a **timed session** checks, against the clock. Its `duration` in the syllabus is
the **time allowed**, not an estimate: the learner works until the time is up. So the questions
must fill that time, no more, no less.

| `kind` | When | Questions | Hints | Solutions |
|---|---|---|---|---|
| `review` | end of a chapter, or a few days after it | mostly earlier questions asked again (`::ref`) and close variants | yes | after each answer |
| `exam` | before a real exam, at its real length | new questions, in the form of the real exam | none | when the time is up |

## 1. Size it to the time

Each question takes an estimated time at a normal pace: **choosing** an answer is quicker than
**building** one.

| Question | Minutes |
|---|---|
| `qcm`, one answer | 1 |
| `qcm`, `multiple=true` | 1.5 |
| `order`, `match` | 2 |
| `numeric`, `text`, `fill` | 3 |
| `free` | 8 |

A question that needs more (a long calculation, a whole problem) says so with `minutes=N`. Add
questions until their total is **90 to 100 % of `duration`**: `boussole validate .` warns
(`P008`) below 80 % or above 110 %.

Example: a 20-minute review → 14 single-choice qcms and 2 numeric exercises (14 + 6 = 20).

## 2. Choose the questions

- **Mostly qcms**: in a given time they cover the most ground. Build answers (`numeric`, `fill`,
  `free`) where the real exam asks for them, or where choosing would give the answer away.
- **Cover every goal** of the sessions concerned at least once, weighted by how much of the
  course each takes.
- **Review**: reuse the lessons' questions with `::ref{id=…}` (retrieving what was learnt is what
  makes it stay), and add variants with new values.
- **Mock exam**: read the **past exams** in the sources first, and mirror them: same kinds of
  questions, same balance between chapters, same difficulty, same length. Write new questions;
  do not reuse the lessons'. Say in the statement when a question is adapted from a past exam.
- Order: easy to hard within a review; as the real exam does within a mock exam.
- Every question still has its `solution`, worked step by step: the learner reads it afterwards.
  In an exam, no `hint`.

## 3. Write it

A timed session has no notion and no `:::example`: an optional short introduction (what it
covers, how long), then the questions.

```markdown
---
id: r19
objectives:
  - Revoir la dualité en conditions d'examen.
terms: [dual, ecarts-complementaires]
---

Révision du chapitre Dualité : 20 minutes, 16 questions.

::ref{id=s11-a1}
::ref{id=s12-a2}

::::qcm{#r19-q1}
…
::::
```

In the syllabus, the session reads:

```yaml
      - id: r19
        title: Révision — Dualité
        kind: review
        duration: 20
        goal: Retrouver les résultats de dualité sans notes, en temps limité.
        file: 19-revision-dualite.md
```

Then run `boussole validate .` until it ends without error, and fix `P008` and `P009` (no notion,
no worked example) for this session.
