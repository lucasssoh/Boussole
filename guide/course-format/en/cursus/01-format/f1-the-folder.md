---
id: f1
objectives:
  - Say what each file of a track holds.
  - Create the smallest valid track and check it with the validator.
terms: [track, syllabus, session, lexicon, document-tracking, identifier, validator]
written_by:
  - model: claude-opus-5-5
    tool: Claude Code
    date: 2026-10-21
---

**Want to write a track yourself, without an agent?** A text editor is enough: a
:term[track]{id=track} is only a folder of Markdown and YAML files.

All along this track, you build one, piece by piece: **"Git basics"**, two sessions and a quick
review. Each session adds a piece.

:::tip
Keep a terminal open beside you: you can type each example and check it right away.
:::

## A folder, a few files {#folder}

**Where does a track start?** With a folder. Here is the one of the running example:

```text
git-basics/
├── subject.yaml            what the track is
├── learner.yaml            how you learn best (optional)
├── sources/                your documents, never changed by Boussole
└── cursus/
    ├── syllabus.yaml       the plan
    ├── lexique.yaml        the terms and their definition
    ├── sources.yaml        document tracking
    ├── assets/             the images of the sessions
    └── 01-first-steps/     one folder per chapter
        └── s1-create-a-repository.md
```

| File | What it holds | Required |
|---|---|---|
| `subject.yaml` | the name, identifier and language of the track | yes |
| `cursus/syllabus.yaml` | the :term[plan]{id=syllabus}: chapters and sessions, in order | yes |
| `cursus/<chapter>/<session>.md` | a :term[session]{id=session}: text and questions | one per session of the plan |
| `cursus/lexique.yaml` | the :term[lexicon]{id=lexicon} | no |
| `cursus/sources.yaml` | :term[document tracking]{id=document-tracking} | as soon as there are documents |
| `learner.yaml` | how you learn best | no |

Boussole ignores every other file: your drafts can stay next to it.

:::example[The subject.yaml of Git basics]
```yaml
format: 1
id: git-basics
name: Git basics
lang: en
description: Create a repository, record your changes and read the history.
```

- `format`: the version of the format, always `1` for now.
- `id`: the :term[identifier]{id=identifier} of the track, in lowercase letters, digits and `-`.
- `name`: the name shown.
- `lang`: the language of the content (`en`, `fr`, `de`…).
- `description`: one or two sentences, optional.
- `authors` and `contributors`: who makes and signs the track (a person, a university with
  `organisation: true`), and who contributed, with their `role`. Optional, shown on the track's
  page.
- `sources`, optional: the folder of the documents, `sources` by default.
:::

::::exercise{#f1-a1 type=match}
Match each file with what it holds.

:::answer
`subject.yaml` => the name, identifier and language of the track
`cursus/syllabus.yaml` => the chapters and sessions, in order
`cursus/lexique.yaml` => the terms and their definition
`cursus/sources.yaml` => what the track did with each document
:::

:::hint
Only one of these files is at the root of the folder: it says what the track is.
:::

:::hint
`lexique` is French for "lexicon"; `sources.yaml` is about the documents of the `sources/` folder.
:::

:::solution
`subject.yaml` describes the track; in `cursus/`, `syllabus.yaml` is the plan, `lexique.yaml`
defines the terms and `sources.yaml` tracks the documents.
:::
::::

::::mcq{#f1-a2}
Which track identifier is valid?

- [x] `git-basics`
- [ ] `Git-Basics`
  > Capital letters are refused: an identifier is written in lowercase.
- [ ] `git_basics`
  > The underscore is refused: only the hyphen `-` joins words.
- [ ] `git basics`
  > Spaces are refused: join the words with hyphens.

:::hint
An identifier only has lowercase letters, digits and hyphens.
:::

:::solution
**`git-basics`**: lowercase letters and a hyphen. The validator refuses capitals, underscores and
spaces (code `F004`).
:::
::::

## The smallest valid track {#minimal}

**What does Boussole need, at the very least?** Two files: `subject.yaml` and
`cursus/syllabus.yaml`. No need to type them, one command creates them:

```
boussole init ~/tracks/git-basics --id git-basics --name "Git basics" --lang en
```

| `boussole init` creates | For |
|---|---|
| `subject.yaml` | with the identifier, name and language given |
| `cursus/syllabus.yaml` | an empty plan: `status: proposed` and `chapters: []` |
| `AGENTS.md`, `CLAUDE.md`, `.boussole/` | the playbooks of agents, and a copy of the format |

It never replaces a file that already exists.

:::note
`.boussole/FORMAT.md` is the complete reference of the format. Keep it at hand: this track shows
the essentials, the format gives every detail.
:::

:::example[Checking the empty track]
In the folder of the track, run the :term[validator]{id=validator}:

```
cd ~/tracks/git-basics
boussole validate .
```

It answers:

```
.: no problem, 0 of 0 sessions written.
```

`no problem`: no error, no warning. The track is valid, even without a session.
:::

::::mcq{#f1-a3 multiple=true}
Which files are needed, at the very least, for the validator to accept a track? (several
answers)

- [x] `subject.yaml`
- [x] `cursus/syllabus.yaml`
- [ ] `cursus/lexique.yaml`
  > The lexicon is optional: a track may not have one yet.
- [ ] `learner.yaml`
  > How you learn best is optional; without it, default values apply.

:::hint
They are the two files `boussole init` creates, apart from the playbooks.
:::

:::solution
**`subject.yaml` and `cursus/syllabus.yaml`.** The lexicon, document tracking and `learner.yaml`
come later, if needed.
:::
::::

## A track in several languages {#languages}

**"What if I want the same track in French?"** You add a **version**: a folder of its own, with
the **same `id`** and its own `lang`.

```
git-basics/
├── en/   subject.yaml: id: git-basics, lang: en
└── fr/   subject.yaml: id: git-basics, lang: fr
```

- Boussole shows both folders as **one track**: the learner picks the language on its page, or
  switches during a session, at the same place.
- Progress is the same in every language.
- **Only the text is translated.** The IDs (sessions, notions `{#…}`, questions) stay the same,
  and each question asks the same thing: same choices, same gaps.
- Documents are not copied: the version points to the original's, `sources: "../en/sources"`.

The validator compares the versions: `F040` when they differ, `F041` for a session not
translated yet.

::::mcq{#f1-a4}
You translate session s1 of "Git basics" into French. What do you keep as it is?

- [ ] The title of the notion `## Creating a repository`
  > The title is translated; its ID `{#init}` is what stays.
- [x] The IDs: `{#init}`, `s1-a1`, and the `id` of the track
- [ ] The accepted answers of a `text` exercise
  > They are in the language of the version: write the French answers.

:::hint
What lets Boussole find the same place in the other language?
:::

:::solution
**The IDs.** The text is translated, titles and answers included; the IDs and what each question
asks stay the same, so that progress and the place in the session follow from one language to
the other.
:::
::::

:::::checkpoint
::::exercise{#f1-c1 type=fill}
Fill in.

In `subject.yaml`, the field [[1]] gives the language of the content and the field [[2]] is
always 1. The plan that `boussole init` creates has the status [[3]].

:::answer
1: choice *lang | name | id
2: choice *format | id | description
3: choice *proposed | approved | empty
:::

:::hint
Read the `subject.yaml` example again, then the table of `boussole init`.
:::

:::solution
`lang` gives the language, `format` the version of the format (`1`). A new plan is `proposed`: a
proposal, to read before writing the sessions.
:::
::::

::::mcq{#f1-c2}
The validator answers `no problem`. What does that mean?

- [x] The track is well written: no error, no warning
- [ ] The answers of your exercises are right
  > The validator checks the form and the teaching contract, not whether your answers are right.
- [ ] The track has at least one session
  > An empty track also gives `no problem`: `0 of 0 sessions written`.

:::hint
Read the validator's answer on the empty track again.
:::

:::solution
**The track is well written.** Whether your answers are right is for you to check: the validator
does not know Git.
:::
::::
:::::
