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
| F001 | missing or invalid field in a YAML file or front matter | add it, as in FORMAT.md §2–5 |
| F003 | the same ID twice | rename the **newer** one; never an ID that already had progress |
| F004 | capital letter, `_` or accent in an ID | lowercase letters, digits, `-` |
| F010 | the file's `id` differs from the syllabus | make the file's `id` match the syllabus |
| F011 | a planned session has no file yet | write it, or say it is still to do |
| F013 | `## Title` without `{#id}` | add an ID to the heading |
| F020 | directive misspelled (often translated: `:::exemple`) or in the wrong place | English names; see where each directive may appear, FORMAT.md §5.5 |
| F021 | exercise without `solution`, or with a wrong `answer` | exactly one `solution`; one `answer` matching the type (none for `free`) |
| F022 | qcm with no `[x]`, or several without `multiple=true` | tick the right choice(s) |
| F023 | gaps `[[n]]` and answer lines differ | one answer line per gap |
| F024, F025 | answer not a number, or invalid `re:` | write `2.5` or `3/4`; fix the regex |
| F026 | container not closed | close it with as many colons as it opened with |
| F027 | unknown or missing attribute | see the directive's attributes in FORMAT.md §5.5–5.6 |
| F030 | reference to an ID that does not exist | add the term to the lexicon, or fix the ID |
| F031 | source file or page not found | use the exact file name in the sources folder and a real page |
| P001–P005 | teaching contract | add the exercise, keep one example, move warmup/practice, add hints, list terms |

Never "fix" an error by deleting content the learner needs, and never edit the sources folder.
