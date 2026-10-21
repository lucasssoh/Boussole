---
id: c4
objectives:
  - Write who wrote, revised or translated a session.
  - Name the authors and contributors of a track.
  - Record where each source comes from, and its licence.
terms: [written-by, contributor, document-tracking, source-note, validator, agent]
written_by:
  - model: claude-opus-5-5
    tool: Claude Code
    date: 2026-10-21
---

:::::warmup
::ref{id=c3-a1}
:::::

## Who wrote this session {#written-by}

**"Did a person write this, or an AI?"** Each session says it.

At the top of a session file, the :term[written by]{id=written-by} lines (`written_by`) name who
wrote it, then who revised or translated it, one line each:

```yaml
written_by:
  - model: claude-opus-5-5
    tool: Claude Code
    date: 2026-10-21
  - name: Inès Haddad
    role: revised
    date: 2026-11-03
```

| Field | Meaning |
|---|---|
| `model` | an AI, by its **exact ID** (`claude-opus-5-5`), not just "an AI" |
| `tool` | the tool that ran it: Claude Code, Codex, Gemini CLI… |
| `name` | a person, in place of `model` |
| `date` | the day of the work |
| `role` | `wrote` (the default), `revised` or `translated` |

You see these lines on the **first step** of the session: look at the one of this session. A
session without them gets the warning `P012` from the :term[validator]{id=validator}.

:::warning
When you revise or translate a session, **add** your line. Never remove or replace the lines
already there: they say who did the work before you.
:::

::::exercise{#c4-a1 type=fill}
Fill in.

Your agent writes a session, then you correct it. The agent's line [[1]]. Your own line has the
role [[2]]. A session with no `written_by` gets the warning [[3]].

:::answer
1: choice *stays as it is | is replaced by yours | is deleted
2: choice *revised | wrote | translated
3: choice *P012 | F039 | F040
:::

:::hint
Read the warning again: what happens to the lines already there?
:::

:::solution
The agent's line **stays**; yours comes after it with the role **revised**. Without any
`written_by`, the validator says `P012`.
:::
::::

## Who makes the track {#authors}

**"Who should I thank for this track?"** Its page says it.

In `subject.yaml`, two lists are shown on the track's page:

- `authors`: who makes and signs the track, people or organisations (`organisation: true`);
- `contributors`: who helped, each with a `role` saying how.

:::example[Crediting the teacher]
Inès builds a track from the lecture notes of Mr Durand, and Tom writes the exercises of
chapter 3:

```yaml
authors:
  - name: Inès Haddad
contributors:
  - name: Mr Durand
    role: lecture notes the track is built from
  - name: Tom Weber
    role: exercises of chapter 3
```

The page of this track, for its part, names one author: Boussole, an organisation.
:::

::::exercise{#c4-a2 type=match}
Inès's track sums up Mr Durand's lecture notes, and an AI wrote its sessions. Match each one with
where they are credited.

:::answer
Inès, who builds and signs the track => authors, in subject.yaml
Her university, which signs the track with her => authors, with organisation: true
Mr Durand, whose notes the track sums up => contributors, with a role
The AI model that wrote the sessions => written_by, in each session
:::

:::hint
`subject.yaml` is about the whole track; `written_by` is about one session.
:::

:::solution
The **authors** sign the track (an organisation with `organisation: true`); the
**contributors** helped, each with a role; the AI that wrote a session is named in that
session's **`written_by`**.
:::
::::

## Where each source comes from {#source-record}

**"My agent found a great article, but it is all rights reserved."** It sums it up instead of
copying it.

:term[Document tracking]{id=document-tracking}, `cursus/sources.yaml`, records each document. For
a document from the web, four more fields:

| Field | Meaning |
|---|---|
| `url` | the address of the page, `https://…` |
| `title` | its title |
| `licence` | its licence, as the page says: `CC BY-SA 4.0`, `all rights reserved`… |
| `retrieved` | the day it was retrieved |

An entry with a `url` but no `licence` gets the warning `F039`.

What goes into the sources folder depends on the licence:

- the licence **allows a copy**: the agent downloads the document;
- it **does not**: the agent writes a :term[source note]{id=source-note}, `web/<name>.md`, its
  own summary of the page, with its address and a few short quotations. Never a copy.

```yaml
- file: web/http-caching.md       # the agent's summary, not a copy
  url: https://example.com/blog/http-caching
  title: HTTP caching, explained
  licence: all rights reserved
  retrieved: 2026-10-21
- file: progit.pdf                # its licence allows a copy
  url: https://git-scm.com/book/en/v2
  title: Pro Git
  licence: CC BY-NC-SA 3.0
  retrieved: 2026-10-21
```

::::mcq{#c4-a3}
Your :term[agent]{id=agent} finds a useful blog article marked "All rights reserved". What does
it put in your sources folder?

- [ ] A copy of the page as a PDF, with its address
  > The licence does not allow a copy, even with the address.
- [x] A source note `web/<name>.md`, its own summary with the address and short quotations
- [ ] Nothing: it uses the article without saying so
  > The track must say where its ideas come from.

:::hint
What does an agent write when the licence does not allow a copy?
:::

:::solution
**A source note**: its own summary, the address and a few short quotations, with an entry in
`cursus/sources.yaml` giving `url`, `title`, `licence: all rights reserved` and `retrieved`.
:::
::::

:::::checkpoint
::::mcq{#c4-c1}
You translate into English a session your classmate wrote. What do you do with its `written_by`?

- [x] Keep her line, and add yours with the role `translated`
- [ ] Replace her line with yours: you wrote the English text
  > She wrote the session: her line stays, yours comes after.
- [ ] Remove `written_by`: the session now has two authors
  > The learner would no longer see who wrote it, and the validator says `P012`.

:::hint
Which lines does a translator remove?
:::

:::solution
**Keep her line and add yours** with `role: translated`. The learner sees both: who wrote the
session, and who translated it.
:::
::::

::::mcq{#c4-c2}
The validator shows `F039` on an entry of document tracking. What is missing?

- [x] Its `licence`
- [ ] Its fingerprint, `sha256`
  > Without a fingerprint, a change goes unnoticed, but that is not `F039`.
- [ ] A `written_by` line
  > `written_by` is about sessions, and its absence gives `P012`.

:::hint
`F039` is about an entry that has a `url`.
:::

:::solution
**The `licence`.** An entry with a `url` says which licence the page is shared under, as its
page says.
:::
::::
:::::
