package io.github.lucasssoh.boussole.core.learn;

import io.github.lucasssoh.boussole.core.learn.check.CheckResult;
import io.github.lucasssoh.boussole.core.learn.check.LearnerResponse;
import io.github.lucasssoh.boussole.core.learn.check.QuestionChecker;
import io.github.lucasssoh.boussole.core.learn.check.ResponseMismatchException;
import io.github.lucasssoh.boussole.core.learn.progress.ExerciseKey;
import io.github.lucasssoh.boussole.core.learn.progress.ExerciseProgress;
import io.github.lucasssoh.boussole.core.learn.progress.ProgressRepository;
import io.github.lucasssoh.boussole.format.model.block.ExerciseBlock;
import io.github.lucasssoh.boussole.format.model.block.McqBlock;
import java.time.Clock;

/** Checks the learner's response to an exercise and records the attempt. */
public final class SubmitAttempt {

    private final CourseLookup lookup;
    private final ProgressRepository progress;
    private final QuestionChecker checker = new QuestionChecker();
    private final Clock clock;

    public SubmitAttempt(ContentRepository content, ProgressRepository progress, Clock clock) {
        this.lookup = new CourseLookup(content);
        this.progress = progress;
        this.clock = clock;
    }

    public AttemptOutcome submit(ExerciseKey key, LearnerResponse response) {
        CheckResult result = switch (lookup.question(key)) {
            case McqBlock mcq -> checker.check(mcq, response);
            case ExerciseBlock exercise -> checker.check(exercise, response);
            default -> throw new ResponseMismatchException(key.exercise() + " is not a question");
        };
        ExerciseProgress after = progress.exercise(key).withAttempt(result.verdict());
        progress.saveExercise(key, after, clock.instant());
        return new AttemptOutcome(result, after);
    }
}
