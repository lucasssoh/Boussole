---
id: f9
objectives:
  - Write a quick review or a mock exam, with `::ref` and new questions.
  - Fit the number of questions to the time allowed.
terms: [quick-review, mock-exam, nudge, notion, worked-example]
written_by:
  - model: claude-opus-5-5
    tool: Claude Code
    date: 2026-10-21
---

:::::warmup
::ref{id=f8-a4}
:::::

## Timed sessions {#timed}

**How do you have the learner review a chapter against the clock?** With a session
`kind: review` or `kind: exam` in the plan. Its file only asks questions: an optional
introduction, then exercises, MCQs and `::ref`s. No :term[notion]{id=notion}, no
:term[worked example]{id=worked-example} (`P009` otherwise).

| | :term[Quick review]{id=quick-review} (`review`) | :term[Mock exam]{id=mock-exam} (`exam`) |
|---|---|---|
| Nudges | available before each answer | not shown |
| Correction | right after each answer | when the time is up, or when the learner ends it |
| `duration` | 5 to 120 minutes | 5 to 240 minutes |

**How do you ask a question of a lesson again without copying it?** With `::ref{id=…}`: the
question, defined elsewhere in the track, shows here as it is. This is also how a warmup goes back
over the previous session. A `::ref` to an identifier that does not exist is an error (`F030`).

:::example[The quick review of Git basics]
```markdown
---
id: r9
objectives:
  - Recall the basic commands without notes, against the clock.
terms: [index]
---

Five minutes, without rereading: the commands of the two sessions.

::ref{id=s1-a1}

::ref{id=s2-a2}

::::mcq{#r9-q1}
What does `git add index.html` do?

- [x] It puts `index.html` in the index, for the next commit
- [ ] It records `index.html` in the history
  > That is the job of `git commit`; `git add` only stages.

:::hint
Think of the :term[index]{id=index}.
:::

:::solution
**`git add`** puts the file in the index; the next `git commit` will record it.
:::
::::
```

Two questions taken from the lessons, one new, and not a single `##` heading.
:::

::::mcq{#f9-a1}
Your quick review holds a heading `## The commands {#commands}`. What does the validator say?

- [x] A warning: a review holds no notion
- [ ] Nothing: the heading has its identifier
  > The heading is well written, but a review only asks questions.
- [ ] An error: a heading without an identifier
  > `{#commands}` is there.

:::hint
In a session, what does a `##` heading start?
:::

:::solution
**A warning** (`P009`): a `##` heading opens a notion, and a timed session has none. A sentence
of introduction is enough.
:::
::::

::::mcq{#f9-a2 multiple=true}
What is true in a mock exam? (several answers)

- [x] Nudges are not shown
- [x] The correction comes at the end
- [ ] An `:::example` can show the method
  > A timed session holds only questions (`P009`).
- [ ] Its duration is only an estimate
  > It is the time allowed: the exam ends when it is up.

:::hint
A mock exam works like a real exam.
:::

:::solution
No :term[nudges]{id=nudge}, and the correction at the end, as in a real exam. No example, and a
duration that is a real time limit.
:::
::::

## The right number of questions {#minutes}

**How many questions for five minutes?** Boussole estimates the time of each question:

| Question | Estimated minutes |
|---|---|
| `mcq` | 1, or 1.5 with `multiple=true` |
| `order`, `match` | 2 |
| `numeric`, `text` | 3 |
| `fill` | 1 per typed gap, 0.5 per list gap, at least 1 |
| `free` | 8 |

The total must fall between 80 % and 110 % of the duration (`P008` otherwise). When a question
takes more or less time than its type, `minutes=N` says so: `::::mcq{#r9-q2 minutes=2}`.

:::example[Counting the minutes of r9]
| Question | Type | Minutes |
|---|---|---|
| `s1-a1` | `mcq` | 1 |
| `s2-a2` | `order` | 2 |
| `r9-q1` | `mcq` | 1 |
| **Total** | | **4** |

The duration is 5 minutes: it takes between 4 (80 %) and 5.5 (110 %). With 4, `r9` passes. One
more `text` question (3 minutes) would make 7: too much, the validator would report `P008`.
:::

::::exercise{#f9-a3 type=numeric unit=minutes}
A review holds 4 single-answer MCQs, 2 `order` exercises and a `fill` with 2 list gaps. How many
minutes does Boussole estimate?

:::answer
9
:::

:::hint
Count type by type: the MCQs, then the `order`s, then the `fill`.
:::

:::hint
4 × 1, then 2 × 2, then 2 × 0.5 (and at least 1 for a `fill`).
:::

:::solution
4 × 1 + 2 × 2 + 2 × 0.5 = 4 + 4 + 1 = **9** minutes.
:::
::::

::::exercise{#f9-a4 type=fill}
Fill in.

For a review of 10 minutes, the questions must add up to between [[1]] and [[2]] minutes;
otherwise, the validator reports [[3]].

:::answer
1: number 8
2: number 11
3: choice *P008 | P009 | F011
:::

:::hint
80 % and 110 % of 10.
:::

:::solution
Between **8** and **11** minutes; outside, **`P008`**.
:::
::::

:::::checkpoint
::::mcq{#f9-c1}
Your review of 5 minutes has three `text` exercises. What do you do?

- [x] Keep one `text` exercise and add two MCQs
- [ ] Keep all three
  > 3 × 3 = 9 minutes: far more than 5.5, the maximum for 5 minutes.
- [ ] Set the duration to 6 minutes
  > 9 minutes is still more than 6.6, the maximum for 6 minutes.

:::hint
A `text` exercise counts 3 minutes, an MCQ 1.
:::

:::solution
**One `text` and two MCQs**: 3 + 1 + 1 = 5 minutes, exactly the duration.
:::
::::
:::::
