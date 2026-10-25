---
id: f6
objectives:
  - Write an MCQ with its feedback, nudges and solution.
  - Write an exercise with a numeric or a text answer.
terms: [nudge, identifier, notion]
prerequisites: [boussole-format.format.f4]
written_by:
  - model: claude-opus-5-5
    tool: Claude Code
    date: 2026-10-21
---

:::::warmup
::ref{id=f5-a3}
:::::

## The MCQ {#mcq}

**How do you know the learner has understood?** By asking a question, right after the example of
the :term[notion]{id=notion}. The simplest is the multiple-choice question, an `::::mcq{#id}`
block:

| Part | How many | Role |
|---|---|---|
| the statement | one, first | the question |
| the choices `- [x]` and `- [ ]` | two or more, at the end of the statement | ticked: right; empty: wrong |
| a feedback `  > …` | under each wrong choice | says why this choice is wrong |
| `:::hint` | one or more | the :term[nudges]{id=nudge}, revealed one at a time before answering |
| `:::solution` | exactly one | why the right answer is right |

- Only one ticked choice, unless `multiple=true`: several right choices, and the interface says so.
- No `:::answer`: the ticked boxes are the answer.
- The :term[identifier]{id=identifier} is unique in the whole track and never reused. The
  convention: `<session>-<letter><number>`, with `a` for a question of a notion, `w` for the
  warmup, `c` for the checkpoint, `p` for the practice.

:::example[The first MCQ of s1]
```markdown
::::mcq{#s1-a1}
What does the `.git` folder created by `git init` hold?

- [x] The whole history of the project
- [ ] A backup copy of your files
  > Git does not copy your folder: it records successive versions.
- [ ] The settings of your editor
  > Your editor keeps its settings elsewhere; `.git` belongs to Git alone.

:::hint
The `.git` folder is the repository itself.
:::

:::solution
**The whole history**: every recorded version, the branches and the settings of the repository
live in `.git`.
:::
::::
```

Feedback is indented by two spaces, under its choice. The right choice needs none, but may have
one.
:::

:::tip
A good wrong choice is a real learner's mistake. Its feedback says where to look, without giving
the right answer away.
:::

This MCQ, on the other hand, passes the validator with warnings:

```markdown
::::mcq{#s1-a3}
Which command creates a repository?

- [x] `git init`
- [ ] `git start`

:::solution
`git init`.
:::
::::
```

::::mcq{#f6-a1 multiple=true}
What is the MCQ `s1-a3` above missing? (several answers)

- [x] A nudge, `:::hint`
- [x] Feedback under the choice `git start`
- [ ] An `:::answer` part
  > An MCQ has none: the ticked boxes are the answer.
- [ ] A second ticked choice
  > One right choice is enough; several only with `multiple=true`.

:::hint
Read the table again: which parts are expected, and how many?
:::

:::hint
The two missing parts give the warnings `P004` and `P010`.
:::

:::solution
**A nudge** (`P004`) and **feedback under the wrong choice** (`P010`), for example: "`git start`
does not exist: the command prepares, it 'initializes'."
:::
::::

::::exercise{#f6-a2 type=text}
An MCQ has two right answers. Which attribute do you add to its opening?

:::answer
multiple=true
multiple
{multiple=true}
:::

:::hint
The attribute says there are "several" right answers.
:::

:::solution
**`multiple=true`** (or `multiple` alone). Without it, two ticked choices are an error (`F022`).
:::
::::

## The exercise {#exercise}

**And when the answer is not a choice?** An `::::exercise{#id type=…}` block: the statement, then
exactly one `:::answer`, one or more `:::hint` and one `:::solution`.

| `type` | What `:::answer` holds | What is accepted |
|---|---|---|
| `numeric` | one value per line; a decimal point, or a fraction `3/4` | the value, within `tolerance` (`0` by default); a comma too |
| `text` | one accepted answer per line; `re:` at the start of a line for a regular expression | ignoring extra spaces and case; `strict=true` keeps case |

`unit=` shows a unit beside the field; the learner does not type it. An answer to type stays short
and precise: a value, a keyword, a command.

:::example[A text exercise in s1]
```markdown
::::exercise{#s1-a2 type=text}
Which command creates a repository in the current folder?

:::answer
git init
:::

:::hint
To prepare something for its first use is to *initialize* it.
:::

:::hint
The command has two words: `git`, then four letters.
:::

:::solution
**`git init`** turns the current folder into a repository, by creating the `.git` folder in it.
:::
::::
```

And a numeric exercise, in `s2`: "How many characters does the identifier of a commit have?",
with `type=numeric unit=characters` and the answer `40`.
:::

::::mcq{#f6-a3}
A learner types `Git Init` for exercise `s1-a2`. Is the answer accepted?

- [x] Yes: a `text` exercise ignores case
- [ ] No: capital letters count
  > Only with `strict=true`, which `s1-a2` does not have.
- [ ] No: the backticks around the command must be typed
  > Backticks only format the text; the learner types the command alone.

:::hint
Read the "What is accepted" column for `text` again.
:::

:::solution
**Yes.** Without `strict=true`, case and extra spaces are ignored: `Git Init` counts as
`git init`.
:::
::::

::::exercise{#f6-a4 type=fill}
Fill in.

To accept 3.14 to within a hundredth, you add the attribute [[1]]; to show "MB" beside the field,
the attribute [[2]]; for capital letters to count, the attribute [[3]].

:::answer
1: choice *tolerance=0.01 | unit=0.01 | strict=0.01
2: choice *unit=MB | tolerance=MB | label=MB
3: choice *strict=true | multiple=true | case=true
:::

:::hint
The three attributes are in the text under the table of types, and in the table itself.
:::

:::solution
`tolerance=0.01` (an absolute gap), `unit=MB` (shown, not typed), `strict=true` (for a `text`
exercise).
:::
::::

:::::checkpoint
::::mcq{#f6-c1}
How many `:::solution` parts does an exercise or an MCQ have?

- [x] Exactly one
- [ ] One per nudge
  > Nudges help before the answer; the solution, only one, explains after it.
- [ ] None for an MCQ
  > An MCQ has its solution too: it shows beside the question, once the learner has answered.

:::hint
How many explanations does the learner read after answering?
:::

:::solution
**Exactly one**, for every exercise and every MCQ (`F021` otherwise). It shows as soon as the
learner has answered.
:::
::::
:::::
