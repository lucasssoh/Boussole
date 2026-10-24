package io.github.lucasssoh.boussole.core.learn;

import io.github.lucasssoh.boussole.core.learn.check.CheckResult;
import io.github.lucasssoh.boussole.core.learn.check.LearnerResponse;
import io.github.lucasssoh.boussole.core.learn.check.QuestionChecker;
import io.github.lucasssoh.boussole.core.learn.check.ResponseMismatchException;
import io.github.lucasssoh.boussole.core.learn.progress.ExerciseKey;
import io.github.lucasssoh.boussole.core.learn.progress.ExerciseProgress;
import io.github.lucasssoh.boussole.core.learn.progress.ProgressRepository;
import io.github.lucasssoh.boussole.core.learn.progress.SessionKey;
import io.github.lucasssoh.boussole.format.model.block.ExerciseBlock;
import io.github.lucasssoh.boussole.format.model.block.McqBlock;
import io.github.lucasssoh.boussole.core.learn.reward.RewardedSubjects;
import io.github.lucasssoh.boussole.core.learn.reward.PointRules;
import io.github.lucasssoh.boussole.core.learn.reward.QuestionScore;
import io.github.lucasssoh.boussole.format.model.TrackLevel;
import io.github.lucasssoh.boussole.format.model.block.Block;
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

    /**
     * {@code session} is the session the learner answers in, which may ask a question of another
     * session again, and {@code language} the version of the track they read it in, whose answer is
     * checked. In a lesson, a question already answered there is refused, in any language (see AnswerOnce).
     */
    public AttemptOutcome submit(ExerciseKey key, LearnerResponse response, Optional<String> session, Optional<String> language) {
        session.ifPresent(id -> refuseASecondAnswer(key, id));
        Block question = lookup.question(key, language);
        CheckResult result = switch (question) {
            case McqBlock mcq -> checker.check(mcq, response);
            case ExerciseBlock exercise -> checker.check(exercise, response);
            default -> throw new ResponseMismatchException(key.exercise() + " is not a question");
        };
        QuestionScore score = QuestionScore.of(question, response, result);
        ExerciseProgress before = progress.exercise(key);
        boolean firstHere = session.map(id -> !progress.firstVerdicts(new SessionKey(key.subject(), id)).containsKey(key.exercise()))
                .orElse(false);
        double points = rewarded.rewards(key.subject())
                ? PointRules.weight(before.attempts() == 0, firstHere) * score.earned() * coefficient(key.subject())
                : 0;
        ExerciseProgress after = before.withAttempt(result.verdict());
        progress.saveExercise(key, after, clock.instant());
        progress.recordAttempt(key, session, result.verdict(), points, clock.instant());
        return new AttemptOutcome(result, after, score, points);
    }

    /** What the level of the track multiplies its points by; 1 when it does not say. */
    private double coefficient(String subject) {
        return lookup.course(subject, Optional.empty()).subject().level().map(TrackLevel::coefficient).orElse(1.0);
    }

    private void refuseASecondAnswer(ExerciseKey key, String session) {
        boolean lesson = AnswerOnce.appliesTo(lookup.session(lookup.course(key.subject(), Optional.empty()), session));
        if (lesson && progress.firstVerdicts(new SessionKey(key.subject(), session)).containsKey(key.exercise())) {
            throw new LearningRuleException(key.exercise() + " is already answered in " + session + ": in a lesson, the first answer is final");
        }
    }
}
