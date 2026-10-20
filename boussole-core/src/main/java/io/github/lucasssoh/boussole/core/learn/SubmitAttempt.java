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
import io.github.lucasssoh.boussole.core.learn.reward.RewardedSubjects;
import io.github.lucasssoh.boussole.core.learn.reward.XpRules;
import java.time.Clock;
import java.util.Optional;

/** Checks the learner's response to an exercise and records the attempt. */
public final class SubmitAttempt {

    private final CourseLookup lookup;
    private final ProgressRepository progress;
    private final QuestionChecker checker = new QuestionChecker();
    private final RewardedSubjects rewarded;
    private final Clock clock;

    public SubmitAttempt(ContentRepository content, ProgressRepository progress, RewardedSubjects rewarded, Clock clock) {
        this.lookup = new CourseLookup(content);
        this.progress = progress;
        this.rewarded = rewarded;
        this.clock = clock;
    }

    /** {@code session} is the session the learner answers in, which may ask a question of another session again. */
    public AttemptOutcome submit(ExerciseKey key, LearnerResponse response, Optional<String> session) {
        CheckResult result = switch (lookup.question(key)) {
            case McqBlock mcq -> checker.check(mcq, response);
            case ExerciseBlock exercise -> checker.check(exercise, response);
            default -> throw new ResponseMismatchException(key.exercise() + " is not a question");
        };
        ExerciseProgress before = progress.exercise(key);
        ExerciseProgress after = before.withAttempt(result.verdict());
        progress.saveExercise(key, after, clock.instant());
        progress.recordAttempt(key, session, result.verdict(), clock.instant());
        int xp = rewarded.rewards(key.subject()) ? XpRules.forAttempt(before.attempts() == 0, result.verdict()) : 0;
        return new AttemptOutcome(result, after, xp);
    }
}
