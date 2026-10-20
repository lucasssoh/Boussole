---
id: g2
objectives:
  - Run `boussole init` on a folder of documents.
  - Follow the agent's steps up to the first session.
terms: [agent, plan, validator]
---

:::::warmup
::ref{id=g1-a1}
:::::

## Preparing the folder {#folder}

A course is **a folder**. Put your documents in it as you like, in sub-folders if you prefer.

Not only a school subject: it works as well for

- a software manual;
- a company guide;
- a programming language.

Then, in a terminal:

```
boussole init ~/courses/Optimisation
```

Boussole adds to the folder:

| File | Role |
|---|---|
| `subject.yaml` | the name and language of the course |
| `cursus/` | the course, which the agent will write |
| `AGENTS.md`, `.boussole/` | the instructions for your :term[agent]{id=agent} |

It never touches your documents.

:::tip
Then open your agent **in that folder** and simply ask: "Build the course."
:::

::::exercise{#g2-a1 type=order}
Put the steps in order, from an empty folder to an open course.

:::answer
1. Put your documents in a folder
2. Run `boussole init` on that folder
3. Ask your agent to build the course
4. Approve the plan it proposes
5. Open the course with `boussole open`
:::

:::hint
A plan can only be approved once the agent has proposed it.
:::

:::solution
Documents first, then `boussole init`, then the agent, which proposes the plan; you approve it;
and `boussole open` opens the course in your browser.
:::
::::

## The plan, then the sessions {#plan}

The agent goes in three steps:

1. **It asks you a few multiple-choice questions**: a quiz after each point? an example first?
   how long a session?
2. **It proposes a :term[plan]{id=plan}**: chapters, sessions, durations, goals, reviews and a
   mock exam.
3. **It writes the sessions one at a time**, running the :term[validator]{id=validator} after
   each.

:::note
Until you approve it, the plan stays **proposed**: the agent writes no session.
:::

You can study the first session while it writes the next: Boussole shows what is new as soon as
it is written.

::::exercise{#g2-a2 type=fill}
Fill in.

The agent proposes the plan with the status [[1]]. It becomes [[2]] only when [[3]] accept it.
To open the course afterwards, the command is [[4]].

:::answer
1: choice *proposed | approved | written
2: choice *approved | proposed | finished
3: choice *you | the agent | Boussole
4: choice *boussole open | boussole init | boussole validate
:::

:::hint
`init` prepares the folder, `validate` checks the course, a single command shows it.
:::

:::solution
`proposed`, then `approved` when **you** accept it; `boussole open` opens the course. `boussole
init` prepares a folder, `boussole validate` checks the format.
:::
::::

:::::checkpoint
::::mcq{#g2-c1}
Your agent wants to write sessions while the plan is still `proposed`. What do you do?

- [ ] Let it, it knows what it does
  > The skills forbid it to write a session before your approval: a plan not approved may still change.
- [x] Read the plan, then approve it or ask for changes
- [ ] Delete `cursus/` and start over
  > No need: just answer its request for approval.

:::hint
Who decides the plan, according to the previous session?
:::

:::solution
Read the plan it shows you, and **approve it** or ask for changes. Sessions are written only
after your approval.
:::
::::
:::::
