---
id: c2
objectives:
  - Say what "all rights reserved" allows, and what a missing licence means.
  - Read a Creative Commons licence from its letters.
  - Find the licence of a web page or a document.
terms: [licence, all-rights-reserved, creative-commons, public-domain]
written_by:
  - model: claude-opus-5-5
    tool: Claude Code
    date: 2026-10-21
---

:::::warmup
::ref{id=c1-a1}
:::::

## All rights reserved {#all-rights-reserved}

**"There is no licence on this page, so it is free?"** The opposite.

A :term[licence]{id=licence} is the author's answer, given in advance, to "may I copy, share or
adapt this?".

| What the document says | What it means |
|---|---|
| "© 2026 A. Martin" or ":term[All rights reserved]{id=all-rights-reserved}" | You may read it and use it for yourself. To copy, share or adapt it, ask. |
| Nothing at all | The same: silence is not a permission. |
| A licence, such as CC BY | Yes in advance, on the conditions it sets. |

Being free to read on the web changes nothing: free to read does not mean free to copy.

::::mcq{#c2-a1}
A blog article you would like to use reads "© 2025 A. Martin. All rights reserved." What does
that allow?

- [ ] Copying it into a track you will share, since it is free to read
  > Free to read is not free to copy: the author reserved those rights.
- [x] Reading it and using it for yourself; copying or sharing it needs the author's consent
- [ ] Nothing at all, not even reading it
  > All rights reserved does not forbid reading: the author published it to be read.

:::hint
Read the first row of the table again.
:::

:::solution
**Reading it and using it for yourself.** To copy it, share it or adapt it, you need the
author's consent.
:::
::::

## Creative Commons and the public domain {#creative-commons}

**"I want my course to be reused, without being asked each time."** That is what
:term[Creative Commons]{id=creative-commons} licences are for: the author says yes in advance,
on conditions. Each condition is a pair of letters.

| Letters | Name | You must… |
|---|---|---|
| BY | Attribution | name the author, the source and the licence, and say what you changed |
| SA | ShareAlike | share what you make from it under the same licence |
| NC | NonCommercial | not use it to make money |
| ND | NoDerivatives | share it unchanged only, never a modified version |

They combine: CC BY, CC BY-SA, CC BY-NC, CC BY-NC-SA, CC BY-ND, CC BY-NC-ND. All of them ask
for BY. A number gives the version, such as 4.0.

Then there is the :term[public domain]{id=public-domain}: no rights left on the work. Either its
protection has expired, after a period set by law (often several decades after the author's
death), or its author gave it up, for instance with **CC0**. Anyone may copy, adapt and share
it; naming the source remains good practice.

:::example[Reading CC BY-NC-SA]
*Pro Git*, a book on Git, is shared under CC BY-NC-SA 3.0. Letter by letter:

- **BY**: name the book and its authors, Scott Chacon and Ben Straub;
- **NC**: no commercial use;
- **SA**: what you make from it and share goes under the same licence.

So you may build a track from it and share it for free, crediting the book, under CC BY-NC-SA.
You may not sell it.
:::

::::exercise{#c2-a2 type=match}
Match each licence element with what it asks of you.

:::answer
BY => name the author and the source
SA => share what you make under the same licence
NC => no commercial use
ND => share it only unchanged
CC0 => no condition at all
:::

:::hint
B for "by" whom, S for "share", N for "no".
:::

:::solution
**BY** names the author, **SA** keeps the same licence, **NC** forbids commercial use, **ND**
forbids modified versions. **CC0** puts the work in the public domain: no condition.
:::
::::

## Finding the licence {#finding-a-licence}

**"Where is the licence written?"** Rarely in big letters. Look:

1. at the **footer** of the web page: a © line, a "Licence" or "License" link;
2. on the **"About"** or **"Terms of use"** page of the site;
3. on the **first or last page** of a PDF;
4. in the **LICENSE** file of a code repository;
5. for a **badge** with the letters CC: it links to its Creative Commons licence.

Much documentation is shared on purpose, under an open licence:

| Source | Licence |
|---|---|
| Wikipedia | CC BY-SA |
| *Pro Git* | CC BY-NC-SA 3.0 |
| The Python documentation | PSF License |
| Many GNU manuals | GNU Free Documentation License |

Nothing found? Treat the document as **all rights reserved**.

::link[The Creative Commons licences, explained by Creative Commons]{url=https://creativecommons.org/share-your-work/cclicenses/}

::::exercise{#c2-a3 type=fill}
Fill in.

You look for the licence of a web page. You look first at [[1]] of the page. A badge with the
letters CC points to [[2]]. If you find nothing, you treat the page as [[3]].

:::answer
1: choice *the footer | the title | the first image
2: choice *a Creative Commons licence | an advertisement | a paid subscription
3: choice *all rights reserved | public domain | CC BY
:::

:::hint
Silence is not a permission.
:::

:::solution
The **footer** first; a CC badge leads to a **Creative Commons licence**; and with no licence
found, the page is **all rights reserved**.
:::
::::

:::::checkpoint
::::mcq{#c2-c1}
A teacher wants to publish their course so that anyone may reuse and adapt it, as long as they
name the teacher and do not sell it. Which licence fits?

- [x] CC BY-NC
- [ ] CC BY-ND
  > ND forbids modified versions: nobody could adapt the course.
- [ ] CC0
  > CC0 asks for nothing, not even the teacher's name, and allows selling.
- [ ] All rights reserved
  > Everyone would have to ask before reusing it: the opposite of what the teacher wants.

:::hint
Name the teacher: which letters? Do not sell it: which letters?
:::

:::solution
**CC BY-NC**: BY to be named, NC to forbid commercial use, and no ND, so adapting is allowed.
CC BY-NC-SA would fit too, if the teacher also wants the adaptations to stay under the same
licence. Before publishing, the teacher checks the rules of their institution, which may hold
rights on the course.
:::
::::
:::::
