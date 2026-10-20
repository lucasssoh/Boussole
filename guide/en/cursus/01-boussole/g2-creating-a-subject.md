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

A subject is **a folder**: put your documents in it as you like, in sub-folders per lecture or
per week if you prefer. Then, in a terminal:

```
boussole init ~/courses/Optimisation
```

Boussole adds `subject.yaml` (the name and language of the subject), the `cursus/` folder that
will hold the course, and the instructions for your :term[agent]{id=agent} (`AGENTS.md`,
`.boussole/`). It never touches your documents.

Then open your agent **in that folder** and simply ask: "Build the course."

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

The agent starts with a few multiple-choice questions (a quiz after each point? an example
first? how long a session?). Then it reads your documents and proposes a :term[plan]{id=plan}:
chapters, sessions, durations, goals, reviews and a mock exam.

The plan stays **proposed** until you approve it. Only then does the agent write the sessions,
one at a time, running the :term[validator]{id=validator} after each. You can study the first
while it writes the next: Boussole shows what is new as soon as it is written.

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
