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

A statement the learner has to decode before they can think teaches nothing. An exercise also
comes back alone, in a warmup or a quick review, with no session around it: it must make sense
by itself.

- **Whole sentences**, as the teacher would say them aloud: a verb, articles, one idea per
  sentence. Never telegraphic notes (« Instance critique de LS pour $m = 3$ : d'abord… »).
  Symbols sit inside a sentence, never instead of it.
- **Ask the question**: the statement ends with what to find, as a question or an instruction
  (« Combien… ? », « Calcule… »). A list of labels after a colon is not a question.
- **Set the context first**: one sentence on the situation and what is being looked for
  (« On cherche une liste de tâches où LS fait aussi mal que possible »), before any value. The
  learner should know what kind of problem this is before reading its numbers.
- **Give the whole setup**: every value, in the units of the course, and every rule the answer
  depends on (the order of a list, how ties are broken, where to start, what is given and what
  is to be found). When the answer follows from a construction of the course, the statement
  describes that construction; it never expects the learner to recall it unnamed. The learner
  should never have to try several configurations to guess which one the question means.
- The words, abbreviations and notations **of the course** (LS, $OPT$, $C_{\max}$) are kept as
  the teacher writes them: learning them is part of the course. What must be spelt out is the
  situation, not the vocabulary.
- **Each gap of a `fill` gets a label that says what goes there**: a short question or phrase,
  with its unit (« Combien de tâches de durée 1 ? »), not a bare symbol.
- **Exactly one answer**: a learner who knows the notion but has never seen this example finds
  one answer only. If two readings are possible, the statement is not finished.
- Same notation as the example just before it.
- **Set the data out, one item per line**, never run into the sentence: a linear program, a
  system, a matrix, a list of points or values, a chain of computations goes in its own block
  (see `seance-write`, *Maths that has a shape*). The statement then reads in three parts: the
  context in a sentence, the data set out, the questions as a list with one gap each. A gap
  stays outside `$…$` and `$$…$$`.

Before and after:

```markdown
Critical instance of LS for $m = 3$: first unit tasks, then one task of length $m$.

- Number of unit tasks: [[1]]
- $C_{\max}$ of LS: [[2]]
```

```markdown
We look for a list of tasks on which the list scheduling algorithm LS does as badly as it can,
with $m = 3$ machines. LS takes the tasks in the order of the list and puts each one on the
machine that is free first. The list starts with enough tasks of length 1 to keep the 3
machines busy for $m - 1 = 2$ units of time, and ends with one task of length $m = 3$.

- How many tasks of length 1 does the list hold? [[1]]
- What total length $C_{\max}$ does LS reach? [[2]]
```

Hints and solutions follow the same rules: whole sentences, nothing left for the learner to
decode.

**Reread** every statement as the learner meets it in a quick review: alone, weeks later. If a
sentence needs reading twice, rewrite it.
- A statement may show a `:::graph` (a network, a weighted graph, a tree) instead of describing
  it in words: the learner reads the figure, the question asks about it. Mark nothing with `!`
  that would give the answer away; the `solution` can repeat the graph with the answer marked.
- An algorithm in a statement is an `:::algorithm`, and the question names the line it is
  about (« À la ligne 4, … »). Code is a fenced block with its language, with `{numbers}` when
  the question names a line; a gap `[[1]]` may sit inside it, in a `fill`.
- A statement never contains its answer, and the answer never depends on an interpretation the
  statement leaves open.

## Answers

- **Recompute every numeric answer with code** (a short script) before writing it. Never trust
  mental arithmetic. Write fractions as `3/4` rather than rounded decimals.
- `text`: list the spellings a learner would reasonably type (with and without article,
  abbreviation and full name). Use `re:` only when a list cannot do it.
- `mcq`: exactly one ticked choice, unless `multiple=true` and the statement says "all that
  apply". **Four choices**; three only when no fourth real mistake exists (never a filler to
  reach four).

## Distractors and feedback

- Each wrong choice of an MCQ is a **real mistake** a learner makes: a confusion between two
  notions, a forgotten step, an off-by-one. Never filler. Take them first from the mistakes the
  documents point out (the teacher's warnings, the corrections of tutorial sheets and past
  exams).
- **Subtle, not obvious:** a wrong choice looks like the right one and differs by one thing (a
  sign, a bound, a condition, a word such as "always" or "at most"); a learner who has only
  skimmed the notion should hesitate. Same form, same length and same register as the right
  choice, so that neither its length nor its wording gives it away.
- **The right choice never explains itself.** A learner soon notices that the one choice with a
  reason after a colon, a « car » or a « parce que » is the right one, and clicks it without
  thinking. Either no choice gives a reason (the reason goes in the `:::solution`), or every
  choice gives one, each wrong choice with the reason a learner would wrongly give: that is the
  *answer + reason* form below. Read the choices without the question: if one stands out, rewrite.
  `boussole validate` warns (P015) when the right choice is the only one with a reason, or much
  longer than the others.

  ```markdown
  - [ ] $x_1$ et $x_2$
  - [ ] $x_4$ seulement
  - [x] $x_3$ et $x_5$ : le sommet est à l'intersection des droites $x_3 = 0$ et $x_5 = 0$
  ```

  ```markdown
  - [ ] $x_1$ et $x_2$ : ce sont les variables de décision
  - [ ] $x_4$ seulement : c'est le seul écart non nul
  - [x] $x_3$ et $x_5$ : ce sont les écarts nuls en $(1, 4)$
  - [ ] $x_1$ et $x_3$ : ce sont les variables qui ont changé depuis $(0, 2)$
  ```
- No "all of the above", "none of the above" or joke choice. The order of the choices does not
  matter: Boussole shuffles them.
- **Every wrong choice gets feedback** (`  > …` under it) that **names the mistake** behind it,
  says why it is wrong and what to look at, without giving the right answer away. The learner who picks it sees it in red,
  with this feedback under it, right after answering (`boussole validate` warns, P010).
- A right choice may have feedback too, to confirm what makes it right.

## Answer + reason

For a notion about an idea rather than a calculation, ask the answer **and why**: a `fill` with
two choice gaps, the second offering the right reason among reasons that are real
misconceptions. A learner who picks the right answer for a wrong reason is caught, and choosing
the reason makes them explain it to themself.

```markdown
::::exercise{#s21-a2 type=fill}
A switch [[1]] forward a frame to every port, because [[2]].

:::answer
1: choice *may | never | always
2: choice *its table does not know the destination address yet | it works at layer 3 | a switch has no table
:::
::::
```

(Solutions and hints as for any exercise; trimmed here.)

## Quick checks

When `learner.yaml` has `quick_check: true`, each notion asks a short `mcq` right after its
explanation: one or two questions answerable in a minute, on the idea just explained, not on
calculation. Its ID follows the « À toi » numbering (`<session>-a1`…).

## Hints in tiers

**Every exercise and every MCQ has hints**, quick checks included (`boussole validate` warns,
P004); only an exam has none. The learner reveals them one at a time, before answering.
For an MCQ, one or two hints; for an exercise, two or three, from vague to precise (fewer in the
final practice and in reviews, where the method has been seen already):

1. **Where to look**: which notion or step applies.
2. **How to start**: the method, the first step, the formula.
3. **Almost there**: the intermediate result, the last step left to the learner.

A hint never gives the final answer.

## Solutions

The learner answers **once**; right after, the interface shows the right answer in green, a
wrong one in red, and the `:::solution` beside the question. So the solution **explains why
the right answer is right**: the reasoning or the worked steps, not just the result, so that a
learner who got it wrong understands what to change. Keep it short for a quick MCQ (two or
three sentences), step by step for a calculation. End with the result in bold. Point to the page
of the documents (or the web source) that gives the method, `:pdf[…]{src=… page=…}`, so that the
learner can check it there.

A learner who got a calculation wrong reads its solution to find **where** they went wrong.
They can only do that if they can redo it line by line:

- **The formula first**, in its general form and in the notation of the course, in a sentence
  that names it (« Formule : $\bar c^{\,j} = c^j - c^I(A^I)^{-1}A^j$ »). The hints may have
  shown it; the solution shows it again, since the learner may have answered without opening
  them.
- **Every number traceable**: say where each value comes from (« la 1re colonne de
  $(A^I)^{-1}$, puisque $A^3 = (1, 0, 0)$ »), then write the formula with the values put in,
  every product written out (`1/3 · 2 + 0 · 2 + 2/3 · 5`), before any simplified sum. A line
  such as `2/3 + 10/3 = 4` hides the step the learner missed.
- **One step per line**, set out in an `aligned` block, the result of each line in bold. When
  the method has stages (compute a column, take a product, subtract), list them first, then
  compute.
- **A decision states its criterion**: when the answer is a verdict (optimal, feasible,
  bounded, adjacent…), give the rule (« en maximisation, tous les coûts réduits hors-base
  $\le 0$ »), then apply it to the values found.
- **Name the expected mistake** when one is likely, in one sentence: what a learner who typed
  the wrong value probably confused (« un écart a un coût $c^j = 0$, mais pas un coût réduit
  nul »).
- With several gaps, the solution follows their order, one part per question, each titled
  (« **1. La solution de base.** »).

Before and after, for the gap `c̄³` of a `fill`:

```markdown
$\bar c^{\,3} = -(1 \cdot \tfrac13 + 0 \cdot 1 - 1 \cdot (-\tfrac13)) = \mathbf{-2/3}$. Tous $\le 0$ : **optimale**.
```

```markdown
**2. Les coûts réduits.** Formule : $\bar c^{\,j} = c^j - c^I(A^I)^{-1}A^j$. Comme
$A^3 = (1, 0, 0)$, $(A^I)^{-1}A^3$ est la 1re colonne de $(A^I)^{-1}$, soit $(\tfrac13, 1, -\tfrac13)$ ;
on la multiplie par $c^I = (1, 0, -1)$, puis on retranche de $c^3 = 0$ :

$$
\bar c^{\,3} = 0 - \left(1 \cdot \tfrac13 + 0 \cdot 1 + (-1) \cdot (-\tfrac13)\right) = 0 - \tfrac23 = \mathbf{-2/3}
$$

**3. La conclusion.** Critère d'optimalité, en maximisation : tous les coûts réduits hors-base
sont $\le 0$. Ici $-2/3 \le 0$ et $-1/3 \le 0$ : la base est **optimale**.
```

The solution of a `free` exercise, where the learner grades themself, ends with **three to six
points to check** (« Ta réponse doit… »): what a right answer contains. Comparing against a list
is far more reliable than comparing against a whole text.

**A free exercise in exam format has a `:::rubric`**: the criteria an exam corrector would mark,
one per item, each with its weight in brackets. The learner ticks those their answer meets, and
the stars follow from the weight ticked.

```markdown
:::rubric
- [2] `FabriqueMotif` est abstraite et déclare `créeMotif()`.
- [1] `Cadre` ne nomme aucune classe concrète de motif.
- [1] Seul le `main` choisit les fabriques.
:::
```

- One criterion is one thing a corrector can see on the page: a class, a line, a step of the
  proof, a justification. Not « bonne réponse », not two checks in one.
- The weights are the points of the marking scheme: when the statement or the past exam gives
  the exercise's points, the weights add up to them; else 1 per criterion, 2 for the heart of it.
- When the exam is written by hand, or the code is written in the learner's own editor, add
  `paper=true` to the exercise: no text area, the learner answers on paper and compares. Give
  `minutes=` the time the exam would allow.
- The solution still explains the reasoning; the rubric does not replace it.

## Check

Run `boussole validate .`: every exercise needs exactly one `solution`, an `answer` of its type
(none for `free`), and the gaps of a `fill` must match its answer. A `rubric` sits only in a
`free` exercise (`F047`), with at least one criterion `- [weight] text` (`F048`).

When you change an exercise in a session written by someone else, add your line to its
`written_by` with `role: revised` (your exact model ID, your tool, today's date).
