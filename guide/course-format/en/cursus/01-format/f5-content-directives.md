---
id: f5
objectives:
  - Choose between example, reminder, callout and term to explain a notion.
  - Open a document, a video or a web page beside the text.
  - Draw a diagram with `graph` or `mermaid`, and write maths.
terms: [worked-example, directive, lexicon, notion]
written_by:
  - model: claude-opus-5-5
    tool: Claude Code
    date: 2026-10-21
---

:::::warmup
::ref{id=f4-a2}
:::::

## Examples, reminders and callouts {#callouts}

**How do you make what matters stand out in an explanation?** With four
:term[directives]{id=directive}:

| Directive | Role | How many |
|---|---|---|
| `:::example[title]` | a :term[worked example]{id=worked-example}: a case solved step by step, solution visible | one per notion |
| `:::recall` | a reminder of a term or a prerequisite, just before it is needed | as many as needed |
| `:::note`, `:::tip`, `:::warning` | a remark, a tip, a mistake to avoid | as many as needed |
| `:::beyond` | what does not come from the documents, and where it comes from | as many as needed |
| `:term[text]{id=…}` | a word whose definition, taken from the :term[lexicon]{id=lexicon}, shows on hover | as many as needed |

For `:term`, `{id=…}` is optional when the text is exactly the term or one of its aliases. Each
term cited is also declared in `terms`, in the front matter (`P005` otherwise).

:::example[The notion "Creating a repository" of s1]
````markdown
## Creating a repository {#init}

:::recall
A :term[repository]{id=repository} is the folder where Git keeps the whole history of a project.
:::

In the folder of the project, `git init` creates a hidden folder, `.git`: that is the repository.

:::example[A repository for your website]
```
cd ~/website
git init
```
Git answers `Initialized empty Git repository in /home/lea/website/.git/`.
:::

:::warning
Do not run `git init` in your home folder: Git would track all your files.
:::
````

The reminder comes before the explanation, the example after it, the warning at the end. Then
comes the question of the notion.
:::

::::exercise{#f5-a1 type=match}
Match each directive with its role.

:::answer
`:::example` => a case solved step by step, solution visible
`:::recall` => a term or a prerequisite, just before it is needed
`:::warning` => a mistake to avoid
`:term` => a definition on hover
:::

:::hint
`recall` means "bring back to mind".
:::

:::hint
Only one of these directives is written inside a sentence.
:::

:::solution
`example` shows a solved case, `recall` reminds what you need to know, `warning` warns, and `term`
shows a definition of the lexicon on hover.
:::
::::

::::mcq{#f5-a2}
Your notion already has an `:::example`. You want to show a second case. What do you do?

- [x] You turn it into a question, right after the example
- [ ] You add a second `:::example`
  > A notion has only one example (`P002`): the learner understands by trying the next case.
- [ ] You put it in a `:::recall`
  > A reminder brings back what is already known; it does not show a new case.

:::hint
What does the learner do right after the example of a notion?
:::

:::solution
**A question.** One example per notion; the next case, the learner solves it, and the solution
of the question shows the method.
:::
::::

::::mcq{#f5-a6}
Your track is built from your teacher's slides. You want to add an analogy of your own that the
slides do not give. Where does it go?

- [x] In a `:::beyond`, which says it does not come from the slides
- [ ] In the explanation, with the rest
  > The learner would take it for what the teacher said: what comes from elsewhere is set apart.
- [ ] Nowhere: a track may only repeat its documents
  > An analogy can help; it is simply shown for what it is.

:::hint
The learner must always know what the teacher said and what was added.
:::

:::solution
Every notion points to the page it relies on (`:pdf`); what comes from elsewhere, an analogy, a
fact of general knowledge, a web source, goes in a `:::beyond` that says where it comes from.
:::
::::

## Documents, videos and links {#media}

**How do you point to a page of the book, a video or a web page?** Three directives open them
beside the text. The label is the caption, or the words of the sentence.

| Directive | Form | Attributes |
|---|---|---|
| `pdf` | line or inline | `src=` a file of `sources/` (required), `page=N` |
| `video` | line | `youtube=` the 11-character video identifier **or** `src=` a video file of `sources/`; `start` and `end` in seconds, `m:ss` or `h:mm:ss` |
| `link` | line or inline | `url=` a whole address, `https://…` (required) |

A web page shows inside Boussole when its site allows it, and otherwise in a window of its own.

:::example[Three references in s1]
```markdown
The book shows it :pdf[on page 32]{src=progit.pdf page=32}.

::video[Git in 100 seconds]{youtube=hwP7WQkmECE start=0:15}

::link[The documentation of git init]{url=https://git-scm.com/docs/git-init}
```

- `:pdf` is inline: it fits in the sentence.
- `::video` is a line on its own; it starts at 15 seconds.
- `::link` is a line on its own: a reference worth reading in full.
:::

::::mcq{#f5-a3}
Which line is valid?

- [x] `::video[Git in 100 seconds]{youtube=hwP7WQkmECE start=0:15}`
- [ ] `:::video[Git in 100 seconds]{youtube=hwP7WQkmECE}`
  > `video` is a line on its own: two colons, not three.
- [ ] `::video[Git]{youtube=hwP7WQkmECE src=git.mp4}`
  > A video comes from YouTube or from a file, never both.
- [ ] `::link[The docs]{url=git-scm.com/docs}`
  > The address must be whole: `https://git-scm.com/docs`.

:::hint
Check the number of colons, then each attribute.
:::

:::solution
**`::video[…]{youtube=hwP7WQkmECE start=0:15}`**: a line, a YouTube identifier, a start at 15
seconds.
:::
::::

::::exercise{#f5-a4 type=fill}
Fill in.

A YouTube video is given with the attribute [[1]], a file of the `sources/` folder with [[2]]. To
start at 2 minutes 10, you write `start=` followed by [[3]].

:::answer
1: choice *youtube | url | src
2: choice *src | file | url
3: choice *2:10 | 210 | 2h10
:::

:::hint
Read the `video` row of the table again.
:::

:::hint
`start` accepts seconds, `m:ss` or `h:mm:ss`.
:::

:::solution
`youtube=…` or `src=…`, and `start=2:10` (or `start=130`, in seconds). `210` would mean 210
seconds, that is 3 minutes 30.
:::
::::

## Diagrams and maths {#diagrams}

**How do you draw a diagram without an image file?** Two ways:

- `:::graph`: nodes and arrows, one line each; Boussole draws it and checks it;
- a `mermaid` code block: a flowchart or a sequence.

| `graph` line | Meaning |
|---|---|
| `a -> b` | an arrow from `a` to `b` |
| `a -- b` | an edge without direction |
| `a -> b : 3` | an arrow with a label |
| `a "My node"` | node `a` shown with this text |
| `… !` | at the end of a line: this element stands out |

The placement is chosen with `{layout=circle}` (default), `row` or `layers`. Above 12 nodes, the
validator warns (`P011`).

Maths is written `$…$` in a sentence and `$$…$$` on lines of their own: Git can name $2^{160}$
different commits. Images use Markdown: `![caption](../assets/branches.png)`.

::::example[The history of s2, as a graph]
You write:

```markdown
:::graph[Three commits, from oldest to newest]{layout=row}
c1 "Add README"
c2 "Add index.html"
c3 "Fix the title" !
c1 -> c2
c2 -> c3
:::
```

Boussole draws:

:::graph[Three commits, from oldest to newest]{layout=row}
c1 "Add README"
c2 "Add index.html"
c3 "Fix the title" !
c1 -> c2
c2 -> c3
:::

The `!` makes the last commit stand out.
::::

::::mcq{#f5-a5}
Which `graph` line does the validator accept?

- [x] `a -> b : 3`
- [ ] `a -> a`
  > An arrow from a node to itself is not drawn: the validator refuses it.
- [ ] `a => b`
  > An arrow is written `->`, an edge without direction `--`.

:::hint
Read the table of `graph` lines again.
:::

:::solution
**`a -> b : 3`**: an arrow from `a` to `b`, with the label `3`.
:::
::::

:::::checkpoint
::::exercise{#f5-c1 type=match}
Match each need with its directive.

:::answer
Show a case solved step by step => example
Open a page of the book => pdf
Cite a web page => link
Draw a small network => graph
:::

:::hint
Directive names are plain English words.
:::

:::solution
`example` for a solved case, `pdf` for a document of `sources/`, `link` for a web page, `graph`
for a diagram of a few nodes.
:::
::::
:::::
