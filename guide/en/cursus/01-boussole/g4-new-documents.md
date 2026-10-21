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

## Document tracking {#record}

**"New slides arrived: what changes?"** You do not have everything on the first day:

- slides arrive week after week;
- a corrected version replaces the old one.

:term[Document tracking]{id=record} (`cursus/sources.yaml`) notes what the track did with each
document, with its **fingerprint**: a code that changes as soon as the file changes.

The :term[quality check]{id=validator} compares this tracking with the folder, sub-folders
included, and warns when a document is:

| Code | The document is… |
|---|---|
| `F033` | new |
| `F034` | changed |
| `F035` | gone |
| `F036` | the same in two places |

To see the list: `boussole sources ~/courses/Optimisation`.

::::exercise{#g4-a1 type=match}
Match each warning with its cause.

:::answer
F033 => a new document, not yet taken in
F034 => a document changed since the track used it
F035 => a tracked document gone from the folder
F036 => the same document in two places
:::

:::hint
The numbers follow the order of the table: new, changed, gone, twice.
:::

:::solution
`F033` new, `F034` changed, `F035` gone, `F036` twice. All four are settled with the agent.
:::
::::

## Adding a document {#adding}

Two moves are enough:

1. **Add the file** to the folder of the track.
2. **Start your :term[agent]{id=agent}** as usual.

The agent starts with the quality check. It sees the new document, reads it and takes it in:

- it **adds** sessions, with new numbers;
- it keeps what you have studied intact;
- it asks for your approval if the plan changes.

::::mcq{#g4-a2}
You add the slides of lecture 7. What becomes of the sessions you have done?

- [ ] They are rewritten to include lecture 7
  > Written sessions stay; only a corrected version of a document would get them corrected.
- [x] They stay as they are; lecture 7 gives new sessions, after them
- [ ] They are renumbered to keep the order
  > Your progress is tied to their numbers: they do not move.

:::hint
What is your progress tied to?
:::

:::solution
They stay: your progress is tied to their numbers. Lecture 7 adds sessions after them, with new
numbers, and the agent shows you what changes in the plan.
:::
::::

:::::checkpoint
::::mcq{#g4-c1}
The quality check reports `F034` for `lecture3.pdf`. What happened?

- [x] `lecture3.pdf` changed since the track used it
- [ ] `lecture3.pdf` is new
  > A new document gives `F033`.
- [ ] `lecture3.pdf` is there twice
  > A duplicate gives `F036`.

:::hint
Read the table of the four warnings again.
:::

:::solution
`F034`: the document **changed** (its fingerprint is no longer the same). The agent looks for
what changed and corrects only the sessions concerned.
:::
::::
:::::
