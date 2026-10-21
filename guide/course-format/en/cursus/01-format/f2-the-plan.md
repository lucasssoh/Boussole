---
id: f2
objectives:
  - Write a plan with its chapters and sessions.
  - Choose the kind, duration and goal of a session, and the status of the plan.
terms: [syllabus, chapter, session, identifier, quick-review, mock-exam]
---

:::::warmup
::ref{id=f1-a2}
:::::

## Chapters and sessions {#chapters}

**How does Boussole know which sessions to show, and in what order?** From the
:term[plan]{id=syllabus}, `cursus/syllabus.yaml`. The order of the file is the order of the track.

The plan is a list of :term[chapters]{id=chapter}, and each chapter a list of sessions:

:::graph[The plan of Git basics]{layout=layers}
p "syllabus.yaml"
p -> c
c "first-steps"
c -> s1
c -> s2
c -> r9
:::

| Chapter field | Required | Meaning |
|---|---|---|
| `id` | yes | identifier, unique in the track |
| `title` | yes | title shown |
| `dir` | yes | folder of the chapter, under `cursus/` |
| `sources` | no | the documents of `sources/` it is built from |

| Session field | Required | Meaning |
|---|---|---|
| `id` | yes | identifier, unique in the chapter |
| `title` | yes | title shown; the session file has no `#` title of its own |
| `duration` | yes | duration in minutes |
| `goal` | yes | one sentence: what the learner can do at the end |
| `file` | yes | the session file, in the folder of the chapter |
| `kind` | no | the kind of session (next notion) |

:::example[The plan of Git basics]
```yaml
status: proposed
chapters:
  - id: first-steps
    title: First steps with Git
    dir: 01-first-steps
    sessions:
      - id: s1
        title: Create a repository
        duration: 15
        goal: Create a Git repository and say what it holds.
        file: s1-create-a-repository.md
      - id: s2
        title: The first commit
        duration: 15
        goal: Record a change with git add and git commit.
        file: s2-first-commit.md
```

- Session `s1` is written in `cursus/01-first-steps/s1-create-a-repository.md`.
- Its full identifier is `git-basics.first-steps.s1`: the track, the chapter, the session.
- Its title, "Create a repository", comes from here: no `#` title in its file.
:::

:::warning
The learner's progress is attached to :term[identifiers]{id=identifier}, never to file names.
Renaming a file loses nothing if `file` follows; changing an `id` loses the progress recorded on
it.
:::

::::exercise{#f2-a1 type=text}
In the track `git-basics`, the chapter `first-steps` holds session `s2`, written in
`s2-first-commit.md`. What is the full identifier of this session?

:::answer
git-basics.first-steps.s2
:::

:::hint
A full identifier has three pieces, separated by dots.
:::

:::hint
Track, then chapter, then session: the file name is not part of it.
:::

:::solution
**`git-basics.first-steps.s2`**: the identifier of the track, of the chapter, then of the
session. The file name is not part of it.
:::
::::

::::mcq{#f2-a2}
Where does Boussole look for the file of session `s1`?

- [x] `cursus/01-first-steps/s1-create-a-repository.md`
- [ ] `cursus/first-steps/s1.md`
  > The folder comes from `dir`, not from the chapter's `id`; the file comes from `file`.
- [ ] `01-first-steps/s1-create-a-repository.md`
  > Chapter folders sit in `cursus/`.

:::hint
Look at the `dir` and `file` fields of the plan.
:::

:::solution
`cursus/`, then the chapter's `dir`, then the session's `file`:
**`cursus/01-first-steps/s1-create-a-repository.md`**.
:::
::::

## Kind, duration and status {#status}

**How do you say a session is a review, and how long it takes?** With `kind` and `duration`.

| `kind` | The session | `duration` |
|---|---|---|
| `lesson` (default) | a lesson: notions and questions | 15 to 120, an estimate of the work, not a limit |
| `review` | a :term[quick review]{id=quick-review}, timed | 5 to 120, the time allowed |
| `exam` | a :term[mock exam]{id=mock-exam}, timed, corrected at the end | 5 to 240, the time allowed |

**And the `status` at the top of the plan?** It says where the plan stands:

- `proposed`: a proposal, to be read. This is the status of a new plan.
- `approved`: the plan is approved; the sessions get written. The validator then reports each
  session whose file is still missing (`F011`, a warning).

::::example[Adding a quick review]
At the end of the chapter, one more session:

```yaml
      - id: r9
        title: Quick review — first steps
        kind: review
        duration: 5
        goal: Recall the basic commands without notes, against the clock.
        file: r9-review.md
```

Once the plan is read, its first line becomes:

```yaml
status: approved
```

:::tip
The `goal` starts with a verb and can be checked: "Record a change", rather than "Understand
Git".
:::
::::

::::exercise{#f2-a3 type=fill}
Fill in.

For a mock exam, the field `kind` is [[1]] and its duration can go up to [[2]] minutes. Without a
`kind` field, the session is [[3]]. A plan that has been read and approved has the status [[4]].

:::answer
1: choice *exam | review | test
2: number 240
3: choice *a lesson | a quick review | a mock exam
4: choice *approved | proposed | ready
:::

:::hint
Read the table of the three kinds of session again.
:::

:::hint
`lesson` is the default value; a mock exam has the longest possible duration.
:::

:::solution
`kind: exam`, up to **240** minutes. Without `kind`, it is **a lesson** (`lesson`). An approved
plan is **`approved`**.
:::
::::

:::::checkpoint
::::mcq{#f2-c1}
A lesson has `duration: 15`. The learner spends 25 minutes on it. What happens?

- [x] Nothing: for a lesson, the duration is an estimate
- [ ] The session stops after 15 minutes
  > Only quick reviews and mock exams are timed.
- [ ] The validator reports an error
  > The validator reads files; it does not see the time the learner spends.

:::hint
Read the `duration` column for `lesson` again.
:::

:::solution
**Nothing.** The duration of a lesson estimates the work, at a normal pace; a lesson ends with its
summary, not with a timer.
:::
::::

::::exercise{#f2-c2 type=numeric unit=minutes}
What is the shortest duration allowed for a lesson?

:::answer
15
:::

:::hint
Quick reviews can go down to 5 minutes; lessons cannot.
:::

:::solution
**15** minutes: a lesson lasts from 15 to 120 minutes.
:::
::::
:::::
