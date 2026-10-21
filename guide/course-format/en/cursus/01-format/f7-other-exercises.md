---
id: f7
objectives:
  - Write a fill-in-the-blanks exercise, with typed gaps, numbers and lists.
  - Write an ordering, a matching or a free-answer exercise.
terms: [nudge, notion]
---

:::::warmup
::ref{id=f6-a2}
:::::

## Fill in the blanks {#fill}

**How do you have the learner complete a sentence, a command or a line of reasoning?** With
`type=fill`. In the statement, `[[1]]`, `[[2]]`… mark the gaps; in `:::answer`, one line per gap:

| Gap | Answer line | The learner |
|---|---|---|
| typed | `1: add` or, with other accepted answers, `1: add \| a` | types a word |
| a number | `2: number 40`, or with a margin, `2: number 3/4 ±0.01` | types a value |
| a list | `3: choice *-m \| -a \| --init` | picks; the right option carries a `*` |

- The accepted answers of a typed gap are separated by a bar `|` with spaces around it.
- The gaps of the statement and those of the answer match one to one (`F023` otherwise).
- A gap may sit in code, inline or in a block, to complete a command or a line of a program:
  `` `ip [[1]] show` ``. Outside the statement of a fill exercise, `[[1]]` shows as written.
- A list cannot show maths: write its options in plain text, with `≤` or `x²` rather than `$…$`.

:::tip
For a proof or a line of reasoning, prefer **list** gaps: the learner picks each step among
plausible mistakes, instead of typing a formula.
:::

:::example[Completing the commands, in s2]
```markdown
::::exercise{#s2-a1 type=fill}
Fill in.

To stage `index.html`, you type `git` followed by [[1]]. To record the commit with its message,
you type `git commit` followed by [[2]]. The identifier of the commit has [[3]] characters.

:::answer
1: add
2: choice *-m | -a | --init
3: number 40
:::

:::hint
The first command "adds" a file to the index.
:::

:::hint
The option for the message is the first letter of *message*.
:::

:::solution
`git add index.html`, then `git commit -m "…"`. The identifier has **40** hexadecimal
characters.
:::
::::
```

Three gaps in the statement, three lines in the answer: one typed, one list, one number.
:::

::::mcq{#f7-a1}
Which answer line offers a list whose right option is `-m`?

- [x] `2: choice *-m | -a | --init`
- [ ] `2: choice -m | -a | --init`
  > No option carries a star: Boussole cannot tell which one is right.
- [ ] `2: -m | -a | --init`
  > Without `choice`, it is a typed gap, where all three answers would be accepted.
- [ ] `2: number -m`
  > `number` expects a numeric value, not an option.

:::hint
A list starts with `choice`, and the right option carries a mark.
:::

:::solution
**`2: choice *-m | -a | --init`**: the word `choice`, then the options, the star in front of the
right one.
:::
::::

::::exercise{#f7-a2 type=fill}
Fill in.

The line `3: number 40` describes a [[1]] gap. The line `2: choice *-m | -a` describes a [[2]]
gap, whose right option carries [[3]]. A line without a keyword, such as `1: add | a`, describes a
[[4]] gap.

:::answer
1: choice *number | list | typed
2: choice *list | number | typed
3: choice *a star | a hash sign | a hyphen
4: choice *typed | list | number
:::

:::hint
Two keywords exist: `number` and `choice`.
:::

:::hint
Without a keyword, the learner types the answer.
:::

:::solution
`number`: a **number** gap; `choice`: a **list** gap, the right option marked with **a star**;
without a keyword: a **typed** gap, where `add` and `a` are accepted.
:::
::::

## Order, pairs and free answer {#order-match}

**And to put steps in order, or link two lists?** Three more types:

| `type` | What `:::answer` holds | The learner |
|---|---|---|
| `order` | a list (`1.` or `-`) in the right order, at least two items | puts the shuffled items back in order |
| `match` | one line per pair, `left => right`, at least two pairs | matches each left side with a right side, picked from a list |
| `free` | no `:::answer` | writes, compares with the solution, then grades themself: got it, partly, not yet |

As for list gaps, the right-hand sides of a `match` show in a list: no `$…$`. `free` is for what
Boussole cannot check: explaining, designing, proving.

:::example[Putting in order, in s2]
```markdown
::::exercise{#s2-a2 type=order}
Put the commands of the first commit in order.

:::answer
1. `git init`
2. `git add index.html`
3. `git commit -m "Add the home page"`
4. `git log`
:::

:::hint
You can only add a file to a repository that exists.
:::

:::solution
You create the repository, stage the file, record the commit, then see it in the history.
:::
::::
```

For a `match`, only the answer changes:

```markdown
:::answer
`git init` => creates the repository
`git add` => stages a file for the next commit
`git commit` => records what is staged
:::
```
:::

::::exercise{#f7-a3 type=match}
Match each exercise type with what it asks the learner to do.

:::answer
`order` => put steps in order
`match` => link two lists
`fill` => complete a sentence or a line of reasoning
`free` => explain, with no automatic check
:::

:::hint
Read each word: order, match, fill, free.
:::

:::solution
`order` puts in order, `match` links, `fill` completes, `free` lets the learner write freely, then
compare with the solution.
:::
::::

::::mcq{#f7-a4}
Which exercise type has no `:::answer` part?

- [x] `free`
- [ ] `order`
  > Its answer is the list in the right order.
- [ ] `match`
  > Its answer is the list of `left => right` pairs.

:::hint
Which one can Boussole not correct on its own?
:::

:::solution
**`free`**: the learner reads the solution and grades themself. All other types have exactly one
`:::answer`.
:::
::::

:::::checkpoint
::::mcq{#f7-c1}
Why write `x²` rather than `$x^2$` in a list option?

- [x] A drop-down list cannot show maths
- [ ] So that the learner types faster
  > In a list, the learner types nothing: they pick.
- [ ] Maths is not allowed anywhere in an exercise
  > Statements, MCQ choices, `order` items and the left-hand sides of a `match` may hold maths.

:::hint
Where do the options of a list gap show?
:::

:::solution
**A drop-down list cannot show maths.** Its options, like the right-hand sides of a `match`, are
written in plain text, with symbols such as `≤`, `x₁` or `u²`.
:::
::::
:::::
