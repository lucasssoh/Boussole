---
name: cursus-discover
description: Gather the sources of a Boussole track when the learner has no documents of their own (boussole new): ask what they want to learn, find open and reliable sources on the web, keep or sum up each one with its address and licence, show them to the learner, then hand over to cursus-plan. Use when the sources folder is empty, or the learner asks to learn something without giving documents.
---

# Build a track from nothing

The learner gave a subject, not documents. You find the documents, and every one of them stays
traceable: the learner can open it, see where it comes from and under which licence. The course
you write afterwards relies on these sources only, never on your memory alone.

## 1. Understand what the learner wants

Read `subject.yaml`: `name` is the subject, `description` (when `boussole new --goal` was used)
the goal in the learner's words. Then ask, with your interactive multiple-choice tool if you have
one, one question at a time, recommended option first:

1. **Where they start**: never touched it / a few notions / comfortable, wants to go further.
2. **What they want to be able to do at the end**: a project (which?), an exam or a
   certification (which?), a job task, curiosity.
3. **How much time**: hours per week, and a deadline if there is one.
4. **Which languages of sources** they read: the language of the track (`lang`), English, both.

Sum up their answers in one sentence in `description` of `subject.yaml` (their goal, their
level, their deadline), and show it to them. If there is no `learner.yaml`, follow
`learner-profile` as well.

## 2. Find the sources

**If you cannot search or read the web, stop here**: tell the learner, and ask them for documents
or links. Never write a track from your memory alone.

Look, in this order of trust:

1. **The official documentation, specification or standard** of the subject (the language's
   tutorial and reference, the RFC, the vendor's guide), at the version the learner will use.
2. **Open courses of universities** (MIT OpenCourseWare and the like) and **open textbooks**
   (OpenStax, LibreTexts, Wikibooks), with their exercises.
3. **Reference works**: encyclopedias (Wikipedia, to start and to cross-check, not as the only
   source), well-known tutorials published by the project itself.
4. Videos are found later, by `video-find`.

Rules:

- **Only addresses you have actually opened.** Never write a URL from memory: an invented or
  outdated address is worse than none. Open each page and check it says what you will rely on.
- **At least two independent sources** for what the track teaches, and one leading source whose
  order the chapters will follow (a tutorial, a textbook).
- Prefer the language of the track, then English; recent versions; sources with exercises.
- Read the **licence** on each page (footer, "License", "Terms"): CC BY, CC BY-SA, public
  domain, an open documentation licence, or all rights reserved.

## 3. Keep each source, traceably

Two cases, by licence:

- **Copies allowed** (an open licence, and a document such as a PDF): download it into the
  sources folder, in a sub-folder named after its origin (`sources/openstax/…`).
- **Anything else**, and every web page: write a **source note**, `sources/web/<name>.md`, never a
  copy of the page:

  ```markdown
  # The Python Tutorial — 4. More Control Flow Tools

  https://docs.python.org/3/tutorial/controlflow.html (PSF License, retrieved 2026-10-21)

  ## What it covers
  - `if`, `for` over any sequence, `range()`, `break` and `else` on loops…

  ## Worth quoting
  > "The `for` statement in Python differs a bit from what you may be used to in C." (§4.2)
  ```

  Your own words, organised by notion, with at most a sentence or two quoted per section.

Then record **every** source in `cursus/sources.yaml` (`docs/FORMAT.md` §8): `file`, `url`,
`title`, `licence`, `retrieved` (today), `status: used`, and the `sha256` that `boussole sources .`
prints. Run `boussole validate .` until there is no `F033` (a source not recorded) and no `F039`
(a licence missing).

## 4. Show the sources to the learner

A table: title, site, licence, what it covers, and which one leads. Ask (interactive tool):
*Keep these sources* / *Remove some* / *Add mine* (a link or a file they have). Adjust, record,
validate again.

## 5. Hand over to cursus-plan

Follow `cursus-plan` with these sources as the documents. Then, in every session:

- **Cite what you rely on**: `:link[…]{url=…}` for the page itself, in the sentence that uses it;
  `::link[…]{url=…}` for a page worth reading in full; `:pdf[…]{src=… page=…}` for a downloaded
  document. The learner opens them beside the text.
- List the source notes and documents of the session in its `sources` front matter.
- Say who wrote each session in its `written_by` (your exact model ID, your tool, today's date).
- When two sources disagree, follow the leading one and say so in a `:::note`.
- Exercises you write yourself are yours: check their answers (run the code, redo the
  computation) before writing the solution.

Tell the learner, at the end, what you could not verify.
