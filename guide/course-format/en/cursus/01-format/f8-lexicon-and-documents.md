---
id: f8
objectives:
  - Write the lexicon of a track and point to it with `:term`.
  - Keep track of the documents of a track.
terms: [lexicon, document-tracking, identifier, validator]
written_by:
  - model: claude-opus-5-5
    tool: Claude Code
    date: 2026-10-21
---

:::::warmup
::ref{id=f7-a4}
:::::

## The lexicon {#lexicon}

**How do you give the definition of a word without breaking the explanation?** You write it once
in the :term[lexicon]{id=lexicon}, `cursus/lexique.yaml`, and each session points to it with
`:term`: the learner reads the definition on hover.

| Field | Required | Meaning |
|---|---|---|
| `id` | yes | identifier, unique in the track |
| `term` | yes | the term as shown |
| `aliases` | no | other spellings, for search |
| `definition` | yes | two or three sentences of Markdown, for someone who has never seen the term |
| `see` | no | the identifiers of related terms |

The link is made in two places of the session:

1. in the front matter, `terms` lists the identifiers used;
2. in the text, `:term[text]{id=…}` shows the definition on hover.

An identifier missing from the lexicon, in `terms`, `:term` or `see`, is an error (`F030`). A
`:term` missing from `terms` gives a warning (`P005`).

:::example[The lexicon of Git basics]
```yaml
- id: repository
  term: Repository
  aliases: [repositories, repo]
  definition: >
    The `.git` folder where Git keeps the whole history of a project. `git init` creates it.
  see: [commit]

- id: commit
  term: Commit
  aliases: [commits]
  definition: >
    A recorded version of the project, with its author, its date and its message.
  see: [repository, index]

- id: index
  term: Index
  aliases: [staging area]
  definition: >
    What will go into the next commit. `git add` puts a file there.
  see: [commit]
```

In session `s2`: `terms: [commit, index]` in the front matter, and in the text
`git add puts the file in the :term[index]{id=index}`. As "index" is exactly the term,
`:term[index]` is enough too.
:::

::::mcq{#f8-a1}
In `s2`, you write `:term[the index]{id=staging}`. What does the validator say?

- [x] An error: `staging` is not the identifier of a term
- [ ] Nothing: `staging area` is an alias of the index
  > `id=` expects the identifier of the term, `index`, not an alias nor part of one.
- [ ] A warning: the term is missing from `terms`
  > That is `P005`, for a term that exists; here, the identifier itself does not exist.

:::hint
What does the `id` field of this term hold, in the lexicon?
:::

:::solution
**An error** (`F030`): `id=` points to an identifier of the lexicon. Here,
`:term[the index]{id=index}`.
:::
::::

::::exercise{#f8-a2 type=fill}
Fill in.

In `lexique.yaml`, the field [[1]] gives the word shown, [[2]] other ways to write it and [[3]]
the identifiers of related terms. In a session, the list [[4]] of the front matter repeats the
terms used.

:::answer
1: choice *term | id | definition
2: choice *aliases | see | term
3: choice *see | aliases | terms
4: choice *terms | objectives | sources
:::

:::hint
Read the table of lexicon fields again.
:::

:::hint
`see` means "see also".
:::

:::solution
`term` is the word shown, `aliases` its other spellings, `see` its neighbours; the front matter
lists the terms of the session in `terms`.
:::
::::

## Document tracking {#documents}

**How will you know, in six months, which document was used for what?** Through
:term[document tracking]{id=document-tracking}, `cursus/sources.yaml`: one entry per document of
the `sources/` folder.

| Field | Meaning |
|---|---|
| `file` | the document, from `sources/`, or a pattern such as `Exams/**` (required) |
| `sha256` | the fingerprint of the document when the track used it |
| `status` | `used` (default), `ignored` (left out) or `pending` (to take in later) |
| `chapters` | the identifiers of the chapters that draw on it |
| `note` | what it is, or why it waits |
| `url`, `title`, `licence`, `retrieved` | for a document taken from the web: its address, its title, its licence and the day you took it |

Without any document, no need for this file: that is the case of this track.

You do not compute the fingerprint by hand: `boussole sources ~/tracks/git-basics` lists the
documents that are new, changed, pending or gone, with the entries to add and their fingerprint.

:::example[Document tracking of Git basics]
```yaml
- file: progit.pdf
  sha256: 2c7d0f…            # given by boussole sources
  status: used
  chapters: [first-steps]
  url: https://git-scm.com/book/en/v2
  title: Pro Git
  licence: CC BY-NC-SA 3.0
  retrieved: 2026-10-21
- file: "drafts/**"
  status: ignored
  note: My preparation notes.
```

The :term[validator]{id=validator} compares this file with the `sources/` folder and warns:

| Code | The document… |
|---|---|
| `F033` | is new, or `pending` |
| `F034` | changed since the track used it |
| `F035` | is listed but is gone |
| `F036` | is there twice, in two places |
| `F039` | has a `url` but no `licence` |
:::

::::exercise{#f8-a3 type=match}
Match each warning with its cause.

:::answer
`F033` => a new document, not tracked yet
`F034` => a document changed since the track used it
`F035` => a tracked document gone from the folder
`F039` => a web address without a licence
:::

:::hint
The first three follow the life of a document: it arrives, it changes, it leaves.
:::

:::solution
`F033` new, `F034` changed, `F035` gone, `F039` a `url` without a `licence`.
:::
::::

::::mcq{#f8-a4}
You put in `sources/` a past exam paper you will work on later. Which status do you give it?

- [x] `pending`
- [ ] `ignored`
  > `ignored` leaves a document out for good; this one will be used later.
- [ ] `used`
  > It is not taken into the track yet.

:::hint
Which of the three means "waiting"?
:::

:::solution
**`pending`**: seen, to be taken in later. Add a `note` saying why it waits.
:::
::::

:::::checkpoint
::::mcq{#f8-c1}
An entry of document tracking has a `url`. Which field must you add to avoid the warning `F039`?

- [x] `licence`
- [ ] `sha256`
  > The fingerprint is advised for every document, but `F039` is about something else.
- [ ] `retrieved`
  > The date is optional.

:::hint
Before reusing a document from the web, what must you know about its rights?
:::

:::solution
**`licence`**: the licence the page is shared under, such as `CC BY-SA 4.0`.
:::
::::
:::::
