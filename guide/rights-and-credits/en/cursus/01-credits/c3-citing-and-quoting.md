---
id: c3
objectives:
  - Tell explaining in your own words from copying.
  - Quote a short passage with its source.
  - Say why a track cites its sources, and how a session does it.
terms: [quotation, licence, track]
written_by:
  - model: claude-opus-5-5
    tool: Claude Code
    date: 2026-10-21
---

:::::warmup
::ref{id=c2-a1}
:::::

## In your own words {#own-words}

**"Can my agent just copy the useful paragraphs?"** Better that it explains them again.

Ideas belong to no one; the wording belongs to its author. So:

| Copying | In your own words |
|---|---|
| The author's sentences, as they are | The idea, explained again |
| A few words swapped for synonyms | Another order, another example |
| The same structure, sentence by sentence | Questions that make you use the idea |

Swapping a few words is not writing in your own words: it is still the author's text, slightly
disguised.

:::tip
It is also better for learning: a :term[track]{id=track} that only copies the course adds
nothing to it. Explaining again, with an example and a question, is what helps you remember.
:::

::::mcq{#c3-a1}
Which of these is writing **in your own words**?

- [ ] Replacing a few words with synonyms in each sentence of the notes
  > The sentences, their order and their structure are still the author's.
- [x] Explaining the idea again with an example of your own, then asking a question about it
- [ ] Copying the paragraph and adding its source below
  > Naming the source is good, but a whole paragraph is still a copy, not your words.

:::hint
Which choice keeps the idea but changes everything else?
:::

:::solution
**Explaining the idea again with your own example.** You keep the idea, which belongs to no
one, and write everything else yourself.
:::
::::

## A short quotation {#quotation}

**"This definition is perfect as it is."** Then quote it.

A :term[quotation]{id=quotation} gives the author's exact words:

- **short**: a sentence or two, not a page;
- **in quotation marks**, so that nobody takes it for your words;
- **with its source**: author, document, page.

Many countries allow short quotations for teaching, study or criticism, without asking, on
conditions of this kind. The details vary: when a passage is long, ask.

:::example[Quoting a definition]
In a session on routing:

> "A router picks the route with the longest matching prefix." (M. Durand, *Networks*, lecture
> 3, page 14)

In other words: among the routes that match the address, the most precise one wins. For
`10.1.2.3`, a route to `10.1.0.0/16` beats a route to `10.0.0.0/8`.
:::

::::exercise{#c3-a2 type=order}
Put the steps of a good quotation in order.

:::answer
1. Pick a short passage worth giving word for word
2. Copy it exactly, in quotation marks
3. Give its source: author, document, page
4. Explain it in your own words right after
:::

:::hint
You cannot give the source of a passage before choosing it.
:::

:::solution
Choose a **short** passage, copy it **exactly** in quotation marks, give its **source**, then
**explain** it: the quotation supports your text, it does not replace it.
:::
::::

## Why a track cites its sources {#why-cite}

**"Where does this come from?"** A track must be able to answer.

Citing lets:

- **you check**: open the original when a point is unclear;
- **the author be credited** for the work they did;
- **the licence be respected**: every Creative Commons :term[licence]{id=licence} asks for BY;
- **anyone find again**, in six months, where each passage comes from.

In a session, two directives open the source beside the text:

```markdown
The router picks the longest prefix
(:pdf[lecture notes, page 14]{src=Networks_Lecture3.pdf page=14}).

::link[The documentation of git init]{url=https://git-scm.com/docs/git-init}
```

- `:pdf` opens a document of your sources folder, at a page if you give one;
- `:link` (in a sentence) or `::link` (on its own line) opens a web page, by its whole address.

::::mcq{#c3-a3}
In a session, you want to cite page 14 of the lecture notes in your sources folder, opened beside
the text. Which directive?

- [x] `:pdf[lecture notes, page 14]{src=Networks_Lecture3.pdf page=14}`
- [ ] `:link[lecture notes]{url=Networks_Lecture3.pdf}`
  > `link` is for a web page, with a whole address starting with `https://`.
- [ ] `::video[lecture notes]{src=Networks_Lecture3.pdf}`
  > `video` plays a video, not a PDF.

:::hint
Which directive is named after the kind of document?
:::

:::solution
**`:pdf`**, with `src` for the file and `page` for the page. `:link` is for web pages, `::video`
for videos.
:::
::::

:::::checkpoint
::::mcq{#c3-c1}
Your agent wants to put a whole page of your teacher's notes in a session, word for word, with
the source. Is that a quotation?

- [ ] Yes, since the source is given
  > A quotation is short: a whole page is a copy, even with its source.
- [x] No, it is too long: better to explain it in its own words and quote a sentence or two
- [ ] Yes, as long as it is in quotation marks
  > Quotation marks do not make a long passage short.

:::hint
How long is a quotation?
:::

:::solution
**No.** A quotation is a sentence or two. For a whole page, the agent explains it in its own
words, quotes only what must be exact, and cites the page with `:pdf`.
:::
::::
:::::
