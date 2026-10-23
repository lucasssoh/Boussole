---
name: learner-profile
description: Ask the learner a few multiple-choice questions (with an interactive question tool such as AskUserQuestion when available) on how they learn best and write learner.yaml, which shapes every session of a Boussole course. Use first, before planning or writing anything, when the subject has no learner.yaml, or when the learner wants to change how sessions are built.
---

# Ask how the learner learns

People learn differently: some want a quick quiz right after each point, some want to try before
seeing an example, some want a closing exercise. `learner.yaml`, at the root of the subject,
records it. The other skills shape the sessions after it, and the validator checks they do.

## When

- `learner.yaml` does not exist: do this **before anything else**, right after the learner's
  first request, then carry on with that request.
- The learner says sessions do not suit them: ask again the questions concerned, update the file.

## Ask

Ask in the language the learner writes in: questions, headers and options.

### With an interactive question tool (preferred)

If your environment has a tool that shows multiple-choice questions the learner answers with the
arrow keys — in Claude Code, **`AskUserQuestion`** — use it, in **two calls of four questions**.
Put the default first, marked as recommended. These tools always let the learner type their own
answer instead ("Other"): use it as the learner's own words.

**First call**

| Header | Question | Options (default first) |
|---|---|---|
| Goal | What do you aim for in this subject? | A good grade · Pass · Among the best · Understand deeply |
| Background | Where do you start from in this field? | Discovering it · Seen the basics in class · Reviewing for the exam |
| Size | How much should a session hold, timed at a normal pace? (You may go faster or slower; a session ends with its closing, not with a timer.) | About 60 min · About 45 min · About 30 min · About 90 min |
| Explaining | How should notions be explained? | Short, to the point · Detailed, step by step |

**Second call**

| Header | Question | Options (default first) |
|---|---|---|
| Order | For each notion, what comes first? | A worked example, then I try · I try first, then the example (put *I try first* first when they answered *Reviewing for the exam*) |
| Quick quiz | A short quiz right after each point is explained? Answering from memory right away is what makes it stay. | Yes · No |
| Closing | How should a session end? | A short quiz on the session · An exercise · Both |
| Videos | Videos beside the text? | A few, where they help · None · As many as useful |

The default order follows where they start from: someone who discovers the field learns more
from a worked example first; someone who already knows the basics, from trying first.

When the learner picked "Other" anywhere, keep their words in `background` or `notes`. Do not ask a
third round; anything else they want to say, they will tell you.

### Without such a tool

Ask in one message. Number the questions, give lettered choices, mark the default, and say they
can answer in one line (`1: … 2b 3c …`) or reply "defaults":

1. **What have you studied so far** in this field, and what feels shaky? (free answer)
2. **Your goal**: a) pass b) a good grade *(default)* c) among the best d) understand deeply
3. **Size of a session**, timed at a normal pace (you may go faster or slower; a session ends
   with its closing, not with a timer): a) about 30 min b) about 45 min c) about 60 min *(default)*
   d) about 90 min
4. **Explanations**: a) short, to the point *(default)* b) detailed, step by step
5. **For each notion**: a) see a worked example, then try *(default when discovering the field)*
   b) try first, then the example *(default when reviewing for the exam)*
6. **A short quiz right after each point** (answering from memory right away is what makes it
   stay): a) yes *(default)* b) no
7. **To close a session**: a) a short quiz *(default)* b) an exercise c) both
8. **Videos**: a) none b) a few, where they help *(default)* c) as many as useful
9. **Anything else** about how you learn? (free, optional)

## Write `learner.yaml`

At the root of the subject, as described in `.boussole/FORMAT.md` §7:

```yaml
background: >
  M1 computer science. Has done linear algebra, never studied duality.
goal: top                # pass | good | top | understand
session_minutes: 60      # 15 to 120
explanations: concise    # concise | detailed
order: example-first     # example-first | try-first
quick_check: true        # a short MCQ right after each notion's explanation
closing: both            # quiz | exercise | both
videos: some             # none | some | many
notes: >
  Retains better with a quick quiz after each point.
```

Map each answer to its field: goal → `goal` (`good`, `pass`, `top`, `understand`), size →
`session_minutes`, explanations → `explanations`, order → `order`, quick quiz → `quick_check`,
closing → `closing` (`quiz`, `exercise`, `both`), videos → `videos` (`some`, `none`, `many`); the
learner's starting point and own words → `background` and `notes`. Then run
`boussole validate .`.

Tell the learner, in one sentence, how their sessions will look. Then offer to keep the profile
for their other subjects: with their agreement, copy it to `~/.config/boussole/learner.yaml`
(`$XDG_CONFIG_HOME/boussole/` if set); `boussole init` copies it into each new subject.

## Changing it later

The file is the learner's. If they change it once sessions exist, tell them which sessions no
longer match (run the validator: `P006`, `P007`) and offer to rework them.
