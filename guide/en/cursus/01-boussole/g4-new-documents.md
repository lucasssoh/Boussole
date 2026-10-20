---
id: g4
objectives:
  - Say how Boussole spots a new or changed document.
  - Know what to ask your agent when a document arrives.
terms: [record, validator, agent]
---

:::::warmup
::ref{id=g3-a1}
:::::

## The record of documents {#record}

You do not have all your documents on the first day: slides arrive lecture after lecture, a
corrected version replaces the old one. The :term[record]{id=record} (`cursus/sources.yaml`)
notes what the course did with each document, with its **fingerprint**, a code that changes as
soon as the file changes.

The :term[validator]{id=validator} compares the record with the folder, sub-folders included, and
warns: a document **new** (`F033`), **changed** (`F034`), **gone** (`F035`), or **the same in two
places** (`F036`). The command `boussole sources ~/courses/Optimisation` lists them.

::::exercise{#g4-a1 type=match}
Match each warning with its cause.

:::answer
F033 => a new document, not yet taken in
F034 => a document changed since the course used it
F035 => a document in the record but gone from the folder
F036 => the same document in two places
:::

:::hint
The numbers follow the order of the text: new, changed, gone, twice.
:::

:::solution
`F033` new, `F034` changed, `F035` gone, `F036` twice. All four are settled with the agent.
:::
::::

## Adding a document {#adding}

Just add the file to the folder of the course, then start your :term[agent]{id=agent} as usual. It
always starts with the validator: it sees the new document, reads it and takes it in. It **adds**
sessions with new numbers, never renumbering or rewriting what you have studied, and asks for
your approval if the plan changes.

::::mcq{#g4-a2}
You add the slides of lecture 7. What happens to the sessions you have done?

- [ ] They are rewritten to include lecture 7
  > No: written sessions stay; only a corrected version of a document would get them fixed.
- [x] They stay as they are; lecture 7 gives new sessions, after them
- [ ] They are renumbered to keep the order
  > Never: your progress is attached to their numbers.

:::hint
What is your progress attached to?
:::

:::solution
They stay: your progress is attached to their numbers. Lecture 7 adds sessions after them, with
new numbers, and the agent shows you what changes in the plan.
:::
::::

:::::checkpoint
::::mcq{#g4-c1}
The validator reports `F034` for `lecture3.pdf`. What happened?

- [x] `lecture3.pdf` changed since the course used it
- [ ] `lecture3.pdf` is new
  > A new document gives `F033`.
- [ ] `lecture3.pdf` is there twice
  > A duplicate gives `F036`.

:::hint
Read the list of the four warnings again.
:::

:::solution
`F034`: the document **changed** (its fingerprint is not the same). The agent looks for what
changed and fixes only the sessions concerned.
:::
::::
:::::
