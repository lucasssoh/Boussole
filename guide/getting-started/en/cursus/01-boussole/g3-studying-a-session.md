---
id: g3
objectives:
  - Move through a session step by step, following the Trail.
  - Answer on the first try, with nudges, and read the explained answer.
terms: [step, trail, guided-mode, first-try, nudge, final-check, streak, quick-review]
written_by:
  - model: claude-opus-5-5
    tool: Claude Code
    date: 2026-10-21
---

:::::warmup
::ref{id=g2-a2}
:::::

## One step at a time {#screens}

**"A whole chapter in one go is discouraging."** So a session moves in :term[steps]{id=step}:

- the goal;
- an explanation, an example or a question per step;
- a wrap-up at the end.

**Next** and **Previous**, or the arrow keys, move from one step to the other.

**"Where am I, and how much is left?"** That is what the :term[Trail]{id=trail} is for, on the left: a zone
per part (warm-up, notions, checkpoint, practice) and a dot per step.

| Dot | Meaning |
|---|---|
| green | seen, or right answer |
| orange | partly right answer |
| red | wrong answer |
| padlock | answer the current question first |

Clicking an open dot goes there.

:::note
:term[Guided mode]{id=guided-mode}: in a lesson, Next waits for your answer to the question
shown. The correction is part of the lesson.
:::

::::mcq{#g3-a1}
On the Trail, a dot has a padlock. What does it mean?

- [ ] This step is optional
  > Every step counts: the padlock only waits for your answer.
- [x] A question before it still waits for your answer
- [ ] The agent has not written this step yet
  > A step not written yet does not appear on the Trail.

:::hint
Read the note on guided mode again.
:::

:::solution
The padlock goes as soon as you have answered the current question, even wrongly: in a lesson,
you move on question after question.
:::
::::

## Answering and understanding {#answers}

**"I click at random until I get it right."** Not here: your answer counts on the :term[first try]{id=first-try},
even if you come back to the session another day. To help you answer well:

- :term[Nudges]{id=nudge}: before answering, some help, then more, without the answer;
- :term[Final check]{id=final-check}: when several answers are right, Boussole asks you to confirm before sending;
- **Explained answer**: right after, the right answer turns green, yours red if it was wrong,
  and a **Why** box explains.

**"I can't see my progress."** Every answer earns **experience**, even a wrong one: trying always
pays. Your :term[streak]{id=streak} grows each day you study.

:::note
:term[Quick reviews]{id=quick-review} and mock exams are timed. In a mock exam, the corrections come at the end, as in a
real exam.
:::

::::mcq{#g3-a2 multiple=true}
What is true? (several answers)

- [x] You can ask for a nudge before answering
- [x] A wrong answer earns experience too
- [ ] You can answer the same question as many times as you like
  > In a lesson, your answer counts on the first try.
- [ ] In a mock exam, the correction shows after each question
  > In a mock exam, it waits for the end, as in a real exam.

:::hint
Two statements are true; read the note on mock exams again.
:::

:::solution
True: the nudge before answering, and experience even for a wrong answer. False: your answer
counts on the first try, and a mock exam keeps its corrections for the end.
:::
::::

:::::checkpoint
::::exercise{#g3-c1 type=match}
Match each dot of the Trail with its meaning.

:::answer
green => seen, or right answer
red => wrong answer
padlock => a question before it waits for your answer
:::

:::hint
The padlock is not a colour: it blocks.
:::

:::solution
**Green**: seen or right; **red**: wrong; **padlock**: answer the current question first.
:::
::::
:::::
