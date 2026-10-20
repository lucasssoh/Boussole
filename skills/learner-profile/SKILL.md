---
name: learner-profile
description: Ask the learner a few multiple-choice questions on how they learn best and write learner.yaml, which shapes every session of a Boussole course. Use first, before planning or writing anything, when the subject has no learner.yaml, or when the learner wants to change how sessions are built.
---

# Ask how the learner learns

People learn differently: some want a quick quiz right after each point, some want to try before
seeing an example, some want a closing exercise. `learner.yaml`, at the root of the subject,
records it. The other skills shape the sessions after it, and the validator checks they do.

## When

- `learner.yaml` does not exist: do this **before anything else**, right after the learner's
  first request, then carry on with that request.
- The learner says sessions do not suit them: ask again the questions concerned, update the file.

## Ask, in one message

Ask in the language the learner writes in. Number the questions, give lettered choices, mark the
default, and say they can answer in one line (`1: … 2b 3c …`) or reply "defaults".

1. **What have you studied so far** in this field, and what feels shaky? (free answer, one or two
   sentences)
2. **Your goal** in this subject: a) pass b) a good grade *(default)* c) among the best
   d) understand deeply, the grade matters less
3. **Length of a session**: a) 30 min b) 45 min c) 60 min *(default)* d) 90 min
4. **Explanations**: a) short, to the point *(default)* b) detailed, step by step
5. **For each notion**: a) see a worked example, then try *(default)* b) try first, then see the
   worked example
6. **A short quiz right after each point is explained**, to check you followed? a) yes b) no
   *(default)*
7. **To close a session**: a) a short quiz on the whole session *(default)* b) an exercise
   c) both
8. **Videos beside the text**: a) none b) a few, where they help *(default)* c) as many as
   useful
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
quick_check: true        # a short qcm right after each notion's explanation
closing: both            # quiz | exercise | both
videos: some             # none | some | many
notes: >
  Retains better with a quick quiz after each point.
```

Map the answers: 2 → `goal`, 3 → `session_minutes`, 4 → `explanations`, 5 → `order`, 6 →
`quick_check`, 7 → `closing`, 8 → `videos`; 1 → `background` and 9 → `notes`, in the learner's own
words. Then run `boussole validate .`.

Tell the learner, in one sentence, how their sessions will look. Then offer to keep the profile
for their other subjects: with their agreement, copy it to `~/.config/boussole/learner.yaml`
(`$XDG_CONFIG_HOME/boussole/` if set); `boussole init` copies it into each new subject.

## Changing it later

The file is the learner's. If they change it once sessions exist, tell them which sessions no
longer match (run the validator: `P006`, `P007`) and offer to rework them.
