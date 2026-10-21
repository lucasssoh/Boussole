---
id: f4
objectives:
  - Recognize the three forms of directive and write their attributes.
  - Nest blocks with the right number of colons.
terms: [directive, notion, nudge]
written_by:
  - model: claude-opus-5-5
    tool: Claude Code
    date: 2026-10-21
---

:::::warmup
::ref{id=f3-a3}
:::::

## Three forms {#forms}

**How do you write something other than text: a callout, a video, a question?** With a
:term[directive]{id=directive}. Its number of colons gives its form:

| Form | Syntax | Example |
|---|---|---|
| Block, over several lines | `:::name[label]{attributes}` … `:::` | `:::tip` … `:::` |
| Line, alone on its line | `::name[label]{attributes}` | `::video[Git in 100 seconds]{youtube=hwP7WQkmECE}` |
| Inline, in a sentence | `:name[text]{attributes}` | `an empty :term[repository]{id=repository}` |

- A block and a line start **at the first column**.
- The label `[…]` and the attributes `{…}` are optional, depending on the directive.
- Names are lowercase English words, whatever the language of the track: `:::example`.
- In a code block, nothing is a directive: that is how this track shows them to you.

**And attributes?** Between braces, separated by spaces:

| Writing | Meaning |
|---|---|
| `{type=text}` | the attribute `type` is `text` |
| `{unit="lines of code"}` | quotes when the value has spaces |
| `{#s1-a1}` | short for `id=s1-a1` |
| `{multiple=true}` | the attribute `multiple` is `true`; `{multiple}` alone says the same |

Each attribute is written only once.

::::example[A tip in session s1]
You write:

```markdown
:::tip[Before you start]
Check that Git is installed: `git --version` prints its version.
:::
```

Boussole shows:

:::tip[Before you start]
Check that Git is installed: `git --version` prints its version.
:::

Three colons: it is a block. The label `[Before you start]` becomes its title.
::::

::::exercise{#f4-a1 type=match}
Match each start of a directive with its form.

:::answer
`:::note` => a block, closed further down by a line of colons
`::video` => a line on its own, with no closing
`:term` => a few words, in the middle of a sentence
:::

:::hint
Count the colons: three, two, one.
:::

:::solution
Three colons: a **block**, which must be closed. Two: a **line**. One: an **inline** directive, in
a sentence.
:::
::::

::::mcq{#f4-a2}
Which exercise opening is valid?

- [x] `::::exercise{#s1-a2 type=text}`
- [ ] `::::exercise{id=s1-a2 #s1-a2 type=text}`
  > `#s1-a2` already means `id=s1-a2`: the attribute is given twice.
- [ ] `::::exercice{#s1-a2 type=text}`
  > Directive names are English words: `exercise`.
- [ ] `::::exercise{#s1-a2 type = text}`
  > No spaces around `=`: spaces separate attributes.

:::hint
Check the name, then each attribute: written once, with no spaces around `=`.
:::

:::solution
**`::::exercise{#s1-a2 type=text}`**: the English name, the identifier as the short `#`, then
`type=text`.
:::
::::

## Nesting blocks {#nesting}

**How do you put a nudge inside a question, and the question inside a checkpoint?** By putting
**more colons on the outside**: one more per level. Each block closes with as many colons as its
opening.

| Level | Block | Colons |
|---|---|---|
| outer | `warmup`, `checkpoint`, `practice` | 5 |
| middle | `mcq`, `exercise` | 4 |
| inner | `hint`, `solution`, `answer` | 3 |

These numbers are enough for almost a whole track. A question asked in a notion, outside any
block, keeps its four colons.

:::example[The checkpoint of s1]
```markdown
:::::checkpoint
::::mcq{#s1-c1}
Which command creates a repository?

- [x] `git init`
- [ ] `git commit`
  > It records a change in a repository that already exists.

:::hint
The command prepares a folder for Git: it "initializes" it.
:::

:::solution
**`git init`** creates the hidden folder `.git`, which will hold the whole history.
:::
::::
:::::
```

- `:::hint` and `:::solution` (3) open and close inside the MCQ.
- `::::` (4) closes the MCQ.
- `:::::` (5) closes the checkpoint.
:::

::::exercise{#f4-a3 type=fill}
Fill in.

In a `:::::warmup`, a question opens with [[1]] colons and its nudge with [[2]]. The
`:::::warmup` block closes with a line of [[3]] colons.

:::answer
1: choice *4 | 3 | 5
2: choice *3 | 4 | 2
3: choice *5 | 3 | 4
:::

:::hint
One more at each level outwards.
:::

:::hint
The innermost block has three colons; the warmup has five.
:::

:::solution
The question: **4**; its :term[nudge]{id=nudge}: **3**; the warmup closes as it opens: **5**.
:::
::::

:::::checkpoint
::::mcq{#f4-c1}
The validator refuses this exercise. Which line is missing?

```markdown
::::exercise{#s1-a2 type=text}
Which command creates a repository?

:::answer
git init
:::

:::hint
It "initializes" a folder for Git.
:::

:::solution
**`git init`**
::::
```

- [x] A `:::` line after the solution
- [ ] A `::::` line at the end
  > The exercise already has its `::::` line: an inner block is still open.
- [ ] A `:::` line after the question
  > The statement is not a block: it has nothing to close.

:::hint
Count the three-colon blocks opened, then those closed.
:::

:::solution
**`:::` after the solution**: `:::answer` and `:::hint` are closed, `:::solution` is not. The
validator reports it with the code `F026`.
:::
::::
:::::
