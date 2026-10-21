---
name: exercise-author
description: Write exercises and multiple-choice questions for a Boussole session, with feedback that teaches, hints in tiers and answers that are verified. Use whenever an exercise, an MCQ or an answer is written or changed.
---

# Write exercises that teach

An exercise is where the learner actually learns. It must be **answerable from what the session
taught**, **checkable by Boussole**, and **right**.

## Pick the type

| The learner must… | Type |
|---|---|
| compute a value | `numeric` (with `unit`, and `tolerance` when rounding is expected) |
| give a precise word, keyword or short string | `text`, with every accepted spelling |
| complete a proof, an algorithm, an equation, a definition | `fill` with **choice gaps** (`3: choice *right \| wrong \| other`), and typed gaps only for precise values |
| complete an address, a table, a short calculation | `fill` with typed or number gaps (`2: number 62`) |
| put steps or layers in order | `order` |
| associate two lists | `match` |
| choose between options, spot a misconception | `mcq` |
| prove, model, design, explain | `free`: the learner compares with the solution |

Prefer a type Boussole can check (`numeric`, `text`, `fill`, `order`, `match`, `mcq`) when the
skill can be checked that way. **Never ask the learner to type a formula** (LaTeX in a web field
is painful and unreliable to check): turn a proof, an algorithm or an equation into a `fill`
where each missing step is a choice among plausible ones, the wrong options being real mistakes.
Typed answers are short and precise: a value, a keyword, an address. Use `free` only for
reasoning nothing else can check, such as writing a whole model.

Options of a choice gap and right-hand sides of a `match` show in drop-down lists, which cannot
draw maths: write them in plain text with Unicode (`≤`, `x₁`, `u²`), never `$…$`.

## Statements

- One clear question. Give every value needed, in the units of the course.
- Same notation as the example just before it.
- A statement may show a `:::graph` (a network, a weighted graph, a tree) instead of describing
  it in words: the learner reads the figure, the question asks about it. Mark nothing with `!`
  that would give the answer away; the `solution` can repeat the graph with the answer marked.
- A statement never contains its answer, and the answer never depends on an interpretation the
  statement leaves open.

## Answers

- **Recompute every numeric answer with code** (a short script) before writing it. Never trust
  mental arithmetic. Write fractions as `3/4` rather than rounded decimals.
- `text`: list the spellings a learner would reasonably type (with and without article,
  abbreviation and full name). Use `re:` only when a list cannot do it.
- `mcq`: exactly one ticked choice, unless `multiple=true` and the statement says "all that
  apply". Three or four choices.

## Distractors and feedback

- Each wrong choice of an MCQ is a **real mistake** a learner makes: a confusion between two
  notions, a forgotten step, an off-by-one. Never filler.
- **Every wrong choice gets feedback** (`  > …` under it) that says **why it is wrong** and what
  to look at, without giving the right answer away. The learner who picks it sees it in red,
  with this feedback under it, right after answering (`boussole validate` warns, P010).
- A right choice may have feedback too, to confirm what makes it right.

## Quick checks

When `learner.yaml` has `quick_check: true`, each notion asks a short `mcq` right after its
explanation: one or two questions answerable in a minute, on the idea just explained, not on
calculation. Its ID follows the « À toi » numbering (`<session>-a1`…).

## Hints in tiers

**Every exercise and every MCQ has hints**, quick checks included (`boussole validate` warns,
P004); only an exam has none. The learner reveals them one at a time, before answering.
For an MCQ, one or two hints; for an exercise, two or three, from vague to precise:

1. **Where to look**: which notion or step applies.
2. **How to start**: the method, the first step, the formula.
3. **Almost there**: the intermediate result, the last step left to the learner.

A hint never gives the final answer.

## Solutions

The learner answers **once**; right after, the interface shows the right answer in green, a
wrong one in red, and the `:::solution` beside the question. So the solution **explains why
the right answer is right**: the reasoning or the worked steps, not just the result, so that a
learner who got it wrong understands what to change. Keep it short for a quick MCQ (two or
three sentences), step by step for a calculation. End with the result in bold.

## Check

Run `boussole validate .`: every exercise needs exactly one `solution`, an `answer` of its type
(none for `free`), and the gaps of a `fill` must match its answer.

When you change an exercise in a session written by someone else, add your line to its
`written_by` with `role: revised` (your exact model ID, your tool, today's date).
