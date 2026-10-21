---
name: cursus-fix
description: Run the Boussole validator on a subject and fix what it reports until there is no error. Use after any change to a course file, and before telling the learner something is done.
---

# Fix what the validator reports

From the subject folder, run:

```
boussole validate .
```

Each line reads `path:line: error|warning CODE message`. The command ends with status `0` when
there is no error. **Repeat until it does.** Then fix the warnings of the files you wrote: they
are about the teaching contract and matter to the learner.

`.boussole/FORMAT.md` §6 lists every code. The usual fixes:

| Code | Usual cause | Fix |
|---|---|---|
| F001 | missing or invalid field in a YAML file or front matter | add it, as in FORMAT.md §2–5; a text holding `: ` goes in quotes |
| F003 | the same ID twice | rename the **newer** one; never an ID that already had progress |
| F004 | capital letter, `_` or accent in an ID | lowercase letters, digits, `-` |
| F010 | the file's `id` differs from the syllabus | make the file's `id` match the syllabus |
| F011 | a planned session has no file yet | write it, or say it is still to do |
| F013 | `## Title` without `{#id}` | add an ID to the heading |
| F020 | directive misspelled (often translated: `:::exemple`) or in the wrong place | English names; see where each directive may appear, FORMAT.md §5.5 |
| F021 | exercise without `solution`, or with a wrong `answer` | exactly one `solution`; one `answer` matching the type (none for `free`) |
| F022 | MCQ with no `[x]`, or several without `multiple=true` | tick the right choice(s) |
| F023 | gaps `[[n]]` and answer lines differ | one answer line per gap |
| F024, F025 | answer not a number, or invalid `re:` | write `2.5` or `3/4`; fix the regex |
| F026 | container not closed | close it with as many colons as it opened with |
| F027 | unknown or missing attribute | see the directive's attributes in FORMAT.md §5.5–5.6 |
| F030 | reference to an ID that does not exist | add the term to the lexicon, or fix the ID |
| F031 | source file or page not found | use the exact file name in the sources folder and a real page |
| F039 | a document from the web without its licence | read the licence on its page and write it in `licence`; when the page gives none, write `all rights reserved` and make sure the file is a source note, not a copy |
| F040 | this version of the track differs from another language: an ID or what a question asks | keep every ID of the other version (sessions, `{#notion}`, exercises) and the same choices, gaps, items; translate only the text |
| F041 | a session written in another language is not translated yet | translate it with `track-translate`, keeping every ID |
| F038 | a video file browsers may not play | ask the learner to convert it to MP4 or WebM (`ffmpeg -i in.mkv out.mp4`), then point `src` to the new file |
| F037 | a `graph` line Boussole cannot read, a node twice, a loop, or places given for some nodes only | one node (`a "Label" @ 0,1`) or edge (`a -> b : 3`) per line; `@` on every node or none; FORMAT.md §5.5 |
| F033 to F036 | a document is new, changed, gone, or the same as another | follow `sources-update`: take it into the course, then record it in `cursus/sources.yaml` |
| P001–P005 | teaching contract | add the exercise, keep one example, move warmup/practice, add hints (MCQs too: one or two, never the answer), list terms |
| P006, P007 | the session does not follow `learner.yaml` | close it as `closing` asks; add the quick MCQ before the example |
| P011 | a `graph` of more than 12 nodes | split it into two graphs, or keep only what the explanation needs |
| P008 | the questions of a review or exam do not fill its time | add or remove questions (estimated times in `exam-build`), or set `minutes=` on a long one |
| P009 | a review or exam holds a notion or a worked example | move it to a lesson; a timed session only asks questions |
| P010 | a wrong MCQ choice has no feedback | add `  > why it is wrong` under it |

Never "fix" an error by deleting content the learner needs, and never edit the sources folder.
