---
id: g2
objectives:
  - Set up a track with `boussole init`.
  - Follow your agent from the plan to the first session.
terms: [agent, plan, validator, link]
---

:::::warmup
::ref{id=g1-a1}
:::::

## Setting up a track {#folder}

**"Where do I start with my AI?"** With a folder.

Put your documents in it, in sub-folders if you like. A track can start from:

- your lecture notes and slides;
- a software manual;
- a company guide;
- the documentation of a programming language.

Then, in a terminal:

```
boussole init ~/courses/Optimisation
```

Boussole adds to the folder:

| File | Role |
|---|---|
| `subject.yaml` | the name and language of the track |
| `cursus/` | the track, which the agent will write |
| `AGENTS.md`, `.boussole/` | the playbooks of your :term[agent]{id=agent} |

Your documents stay where they are.

:::tip
Open your agent **in that folder** and ask it: "Build the track."
:::

**"What if I have no documents?"** Start from nothing:

```
boussole new "Python" --goal "automate my work"
```

Your agent asks what you want to be able to do, looks for open sources (official documentation,
university courses, open textbooks) and shows them to you before making the plan. Every source
keeps its address and licence; in the sessions, a :term[link]{id=link} opens it beside the text.

::::exercise{#g2-a1 type=order}
Put the steps in order, from an empty folder to an open track.

:::answer
1. Put your documents in a folder
2. Run `boussole init` on that folder
3. Ask your agent to build the track
4. Approve the plan it proposes
5. Open the track with `boussole open`
:::

:::hint
You can only approve a plan once the agent has proposed it.
:::

:::solution
Documents first, then `boussole init`, then the agent, which proposes the plan; you approve it;
and `boussole open` opens the track in your browser.
:::
::::

## The plan, then the sessions {#plan}

**"What if the AI goes in a direction I did not choose?"** It asks you first.

The agent goes in three steps:

1. **Your learning style.** A few multiple-choice questions: a question after each notion? an
   example first? how long a session?
2. **The plan for approval.** Chapters, sessions, durations, goals, quick reviews and a mock
   exam: that is the :term[plan]{id=plan}.
3. **The sessions.** One at a time, each one checked by the :term[validator]{id=validator}.

:::note
Until you approve it, the plan stays **proposed**: no session is written.
:::

You can start the first session while the agent writes the next: it shows up as soon as it is
ready.

::::exercise{#g2-a2 type=fill}
Fill in.

The agent proposes the plan with the status [[1]]. It moves to [[2]] only when [[3]] approve it.
To open the track afterwards, the command is [[4]].

:::answer
1: choice *proposed | approved | written
2: choice *approved | proposed | finished
3: choice *you | the agent | Boussole
4: choice *boussole open | boussole init | boussole validate
:::

:::hint
`init` prepares the folder, `validate` checks the track, a single command shows it.
:::

:::solution
`proposed`, then `approved` when **you** approve it; `boussole open` opens the track. `boussole
init` prepares a folder, `boussole validate` checks the track.
:::
::::

:::::checkpoint
::::mcq{#g2-c1}
Your agent wants to write sessions while the plan is still `proposed`. What do you do?

- [ ] Let it go ahead, it knows what it is doing
  > A plan not yet approved can still change: the sessions wait for your approval.
- [x] Read the plan, then approve it or ask for changes
- [ ] Delete `cursus/` and start again
  > No need: just answer its request for approval.

:::hint
Who decides on the plan, according to the previous session?
:::

:::solution
Read the plan it shows you and **approve it** or ask for changes. Sessions are only written after
your approval.
:::
::::
:::::
