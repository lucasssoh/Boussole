# boussole-core

The domain: what a learner does with a course. No framework, no database, no network.

**It knows how to** open a course and a session, check a response to an MCQ or an exercise,
reveal hints one at a time, show a solution once it is allowed, and record a finished session.

**It does not** read files, store anything or speak HTTP: it asks its ports,
`ContentRepository` and `ProgressRepository`, implemented in `boussole-adapters`.

```java
SubmitAttempt submit = new SubmitAttempt(content, progress, Clock.systemDefaultZone());
AttemptOutcome outcome = submit.submit(new ExerciseKey("oc", "s34-a1"), new ChoicesResponse(Set.of(1)));
outcome.result().verdict();   // CORRECT, PARTLY or WRONG
```

| Package | Contents |
|---|---|
| `learn` | the use cases, one class each, and the `ContentRepository` port |
| `learn.check` | the responses (`LearnerResponse`), a `ResponseChecker` per kind of question, `ExpectationMatcher` |
| `learn.progress` | `ExerciseProgress`, the keys, the `ProgressRepository` port |

Tests: `QuestionCheckerTest` and `ExpectationMatcherTest` for checking; `LearningTest` runs the
use cases on [`examples/reseaux`](../examples/reseaux) with progress in memory.
