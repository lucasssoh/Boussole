---
id: c5
objectives:
  - Say who is responsible when a track is shared.
  - Sort what you can usually share from what needs the author's consent.
  - Credit the work of others, and say honestly what an AI wrote.
terms: [track, licence, contributor, written-by]
prerequisites: [boussole-credits.credits.c2]
written_by:
  - model: claude-opus-5-5
    tool: Claude Code
    date: 2026-10-21
---

:::::warmup
::ref{id=c4-a3}
:::::

## Who shares, answers for it {#sharing}

**"A classmate wants my track. Can Boussole send it?"** Boussole sends nothing: you do.

A :term[track]{id=track} is a folder on your machine. It holds:

- the sessions, the plan and the lexicon, in `cursus/`;
- **the documents** it was built from: perhaps your teacher's slides.

Send the folder, and you send the documents with it.

Boussole is like a video player. Nothing technically stops you from uploading a film you bought
to a video site; the law does, and the one who uploads it answers for it. Same here:

- between two people, each shares what they choose, and **is responsible for it**;
- sharing documents you have no right to share is the responsibility of **the one who shares
  them**, not of Boussole;
- whoever packages and publishes a track that holds documents **accepts responsibility for
  having the right** to share them.

:::note
Boussole plans a way to package and share tracks. The principle above will apply to it.
:::

::::mcq{#c5-a1}
You compress your track folder, your teacher's slides included, and post it in the class group
chat. Who is responsible for sharing the slides?

- [x] You, who shared them
- [ ] Boussole, which built the track
  > Boussole works on your machine and sent nothing: you posted the folder.
- [ ] Your agent, which wrote the sessions
  > The agent wrote sessions; the choice to send the slides was yours.

:::hint
Who posted the folder?
:::

:::solution
**You.** The one who shares answers for what they share. Before sending documents that are not
yours, ask their author.
:::
::::

## What you can usually share {#what-to-share}

**"So what can I send?"** Sort the folder.

| Usually yes | Usually not, without consent |
|---|---|
| The questions you wrote | Your teacher's slides and lecture notes |
| Summaries in your own words | Exercise sheets and past exams |
| Documents under a :term[licence]{id=licence} that allows it, on its conditions | Scans of textbook pages |
| Works in the public domain | Web pages under all rights reserved |

Two points of care:

- a track that follows a teacher's course step by step is still built on their work: **tell
  them, and credit them**;
- left out of the folder, a document can no longer be opened by the sessions that cite it: the
  person you share with adds their own copy, for instance the one the teacher gave them.

:::example[Sorting a track before sharing it]
Inès asked Mr Durand, who answered: "My notes stay in the class; your questions, share them as
you wish."

She shares with a friend from another school:

- **kept**: her sessions and questions, and *Pro Git* (CC BY-NC-SA 3.0), credited, for free;
- **removed**: Mr Durand's lecture notes;
- **in `subject.yaml`**: Mr Durand among the contributors, with his role.
:::

::::mcq{#c5-a2 multiple=true}
Which of these can you **usually** share without asking?

- [x] The questions you wrote yourself
- [x] A chapter of *Pro Git*, under CC BY-NC-SA, credited and for free
- [ ] Your teacher's slides
  > They belong to your teacher: ask first.
- [ ] Scans of your textbook
  > The textbook belongs to its authors and publisher.

:::hint
Read the "Usually yes" column again.
:::

:::solution
**Your own questions**, and **a document whose licence allows it**, on its conditions (here:
credited, no commercial use, same licence). Your teacher's slides and the textbook need their
authors' consent.
:::
::::

## Respecting the work of others {#respect}

**"Who wrote this course, in the end?"** Whoever reads your track should be able to tell.

Writing a course takes hours. Credit is what keeps the people who write them writing, and
sharing. In practice:

- **credit** the teachers and :term[contributors]{id=contributor} whose work you used;
- **keep** someone else's :term[written by]{id=written-by} line when you revise their session;
- **never present** a generated or copied course as your own work;
- **say what an AI wrote**: its exact ID in `written_by`, and your own line when you revised it.

:::tip
Being honest about an AI costs nothing: "an AI wrote it, I checked it" is useful information for
whoever studies the track.
:::

::::mcq{#c5-a3}
You share a track whose sessions an AI wrote and you then corrected. How should it read?

- [x] In each session, the model's line with `wrote`, then yours with `revised`
- [ ] Only your name, with `wrote`: you checked everything
  > That hides the AI's work: the learner can no longer tell who wrote what.
- [ ] No `written_by` at all, to keep it simple
  > The learner no longer sees who wrote the session, and the validator says `P012`.

:::hint
What does each role say: `wrote`, `revised`?
:::

:::solution
**The model's line, then yours.** The AI wrote, you revised: both are true, and both are shown
on the first step of the session.
:::
::::

:::::checkpoint
::::mcq{#c5-c1}
Your teacher agreed that you share your track with the class, not online. A month later, someone
offers to post it on a public website. What do you do?

- [x] Ask the teacher again: the consent was for the class only
- [ ] Post it: the teacher has already agreed
  > The teacher agreed for the class, not for the whole web.
- [ ] Post it, and let the website remove it if there is a problem
  > The one who shares answers for it: the website does not take the responsibility for you.

:::hint
What exactly did the teacher agree to?
:::

:::solution
**Ask again.** A consent covers what was asked: "within the class" is not "online". Many
teachers agree, often on a licence of their choice.
:::
::::
:::::
