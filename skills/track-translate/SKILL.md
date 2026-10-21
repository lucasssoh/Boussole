---
name: track-translate
description: Write a version of a Boussole track in another language, keeping every ID so that progress and the place in a session carry over between languages. Use when the learner asks to follow a track in another language, or a contributor adds a language; also to finish the sessions that boussole validate reports with F041.
---

# Translate a track

A version of a track in another language is a folder of its own, with the same `id` and its
own `lang` (`docs/FORMAT.md` §9). Boussole shows the versions as one track: the learner switches
language in a session and lands at the same place, with the same progress. That works only if
**every ID stays the same**: you translate text, never structure.

## 1. Set up the version

1. Ask the learner which language (interactive tool if you have one), and check it is not
   already there: `boussole validate .` lists the other versions it finds when they differ.
2. Create the folder next to the track's: `python/fr` beside `python/en` when the track already
   lives in a language folder, else `python-en` beside `python`. Never write inside the
   original's folder.
3. Copy `subject.yaml` and `cursus/` into it. Change `lang`, translate `name` and `description`.
   Keep `id`.
4. Documents: do not copy them. Point `sources` to the original's folder, relative to the new
   one (`sources: "../python/sources"`), so that `:pdf` and `::video{src=…}` still find them.
   Copy `cursus/sources.yaml` as it is.

## 2. Translate

Translate, in every file: titles, goals, objectives, explanations, examples, statements,
choices, feedback, hints, solutions, captions, lexicon terms and definitions, labels of
`link`, `video`, `pdf` and `graph` nodes.

**Never change**:

- any ID: sessions (`id`, file names may stay), chapters, notions (`## Title {#id}` — translate
  the title, keep `{#id}`), exercises and MCQs (`{#s3-a1}`), lexicon IDs (`id:` of a term and
  `{id=…}` of `:term`), `::ref`;
- the order of sessions, notions and questions;
- what a question asks: the same choices in the same order, the same ones ticked `[x]`, the same
  gaps `[[N]]` and their kind, the same items to order and pairs to match, the same numbers;
- code, commands, file names, maths and URLs, unless they are prose.

The accepted answers of a typed gap or a `text` exercise are written in the new language: list
the usual spellings (`1: routing | the routing`). A number stays the same.

Use the terms of the field as people write them in that language, not a word-for-word
translation; when the sources use another language, give the original term once in brackets.

## 3. Check

Run `boussole validate <new folder>` until there is no error: `F040` points at any ID or
question that differs from the original. Then add the version for the learner:
`boussole open <new folder>`. In Boussole, open a session of the track and switch language in
its bar: the same step must show, in the new language.

Translate one session at a time if the track is long: a session not yet translated is reported
as `F041`, a warning, and the learner can follow the translated ones already.
