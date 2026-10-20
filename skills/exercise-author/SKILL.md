---
name: exercise-author
description: Write exercises and multiple-choice questions for a Boussole session, with feedback that teaches, hints in tiers and answers that are verified. Use whenever an exercise, a qcm or an answer is written or changed.
---

# Write exercises that teach

An exercise is where the learner actually learns. It must be **answerable from what the session
taught**, **checkable by Boussole**, and **right**.

## Pick the type

| The learner must… | Type |
|---|---|
| compute a value | `numeric` (with `unit`, and `tolerance` when rounding is expected) |
| name a concept, give a word, a short expression | `text`, with every accepted spelling |
| complete a formula, an address, a sentence | `fill` with gaps `[[1]]`, `[[2]]` |
| put steps or layers in order | `order` |
| associate two lists | `match` |
| choose between options, spot a misconception | `qcm` |
| prove, model, design, explain | `free`: the learner compares with the solution |

Prefer a type Boussole can check (`numeric`, `text`, `fill`, `order`, `match`, `qcm`) when the
skill can be checked that way. Use `free` for reasoning that cannot.

## Statements

- One clear question. Give every value needed, in the units of the course.
- Same notation as the example just before it.
- A statement never contains its answer, and the answer never depends on an interpretation the
  statement leaves open.

## Answers

- **Recompute every numeric answer with code** (a short script) before writing it. Never trust
  mental arithmetic. Write fractions as `3/4` rather than rounded decimals.
- `text`: list the spellings a learner would reasonably type (with and without article,
  abbreviation and full name). Use `re:` only when a list cannot do it.
- `qcm`: exactly one ticked choice, unless `multiple=true` and the statement says "all that
  apply". Three or four choices.

## Distractors and feedback

- Each wrong choice of a qcm is a **real mistake** a learner makes: a confusion between two
  notions, a forgotten step, an off-by-one. Never filler.
- Each wrong choice gets feedback (`  > …` under it) that says **why it is wrong** and what to
  look at, without giving the right answer away.

## Hints in tiers

Two or three `:::hint`, from vague to precise:

1. **Where to look**: which notion or step applies.
2. **How to start**: the method, the first step, the formula.
3. **Almost there**: the intermediate result, the last step left to the learner.

A hint never gives the final answer.

## Solutions

The `:::solution` is a worked solution, step by step, not just the result: the learner reads it
after an attempt to understand what went wrong. End with the result in bold.

## Check

Run `boussole validate .`: every exercise needs exactly one `solution`, an `answer` of its type
(none for `free`), and the gaps of a `fill` must match its answer.
