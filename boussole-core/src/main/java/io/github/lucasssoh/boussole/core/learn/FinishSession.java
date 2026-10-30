package io.github.lucasssoh.boussole.core.learn;

import io.github.lucasssoh.boussole.core.learn.check.Verdict;
import io.github.lucasssoh.boussole.core.learn.progress.FirstAnswer;
import io.github.lucasssoh.boussole.core.learn.progress.ProgressRepository;
import io.github.lucasssoh.boussole.core.learn.progress.SessionKey;
import io.github.lucasssoh.boussole.core.learn.reward.PointRules;
import io.github.lucasssoh.boussole.core.learn.reward.RewardedSubjects;
import io.github.lucasssoh.boussole.format.model.CourseSession;
import io.github.lucasssoh.boussole.format.model.block.ExerciseBlock;
import io.github.lucasssoh.boussole.format.model.block.ExerciseRefBlock;
import io.github.lucasssoh.boussole.format.model.block.McqBlock;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Records that the learner reached the end of a session, and the time they actually spent on it;
 * returns the bonus it earned: the first time a session is finished without a single mistake,
 * every question right at the first answer given in it, hooks aside.
 */
public final class FinishSession {

    private final CourseLookup lookup;
    private final ProgressRepository progress;
    private final RewardedSubjects rewarded;
    private final Clock clock;

    public FinishSession(ContentRepository content, ProgressRepository progress, RewardedSubjects rewarded, Clock clock) {
        this.lookup = new CourseLookup(content);
        this.progress = progress;
        this.rewarded = rewarded;
        this.clock = clock;
    }

    public double finish(SessionKey key, Duration spent) {
        CourseSession session = lookup.session(lookup.course(key.subject(), Optional.empty()), key.session());
        boolean finishedBefore = progress.finishedSessions(key.subject()).contains(key.session());
        double bonus = rewarded.rewards(key.subject()) && !finishedBefore && flawless(key, session) ? PointRules.flawlessBonus(pointsIn(key)) : 0;
        progress.saveSessionFinished(key, clock.instant(), spent.isNegative() ? Duration.ZERO : spent, bonus);
        return bonus;
    }

    private boolean flawless(SessionKey key, CourseSession session) {
        List<String> questions = session.session().blocks()
                .flatMap(block -> switch (block) {
                    case McqBlock mcq when !mcq.hook() -> Stream.of(mcq.id());
                    case ExerciseBlock exercise when !exercise.hook() -> Stream.of(exercise.id());
                    case ExerciseRefBlock ref -> Stream.of(ref.id());
                    default -> Stream.<String>empty();
                })
                .distinct()
                .toList();
        Map<String, FirstAnswer> first = progress.firstAnswers(key);
        return !questions.isEmpty() && questions.stream().allMatch(id -> first.containsKey(id) && first.get(id).verdict() == Verdict.CORRECT);
    }

    private double pointsIn(SessionKey key) {
        return progress.attemptHistory().stream()
                .filter(attempt -> attempt.subject().equals(key.subject()) && attempt.session().equals(Optional.of(key.session())))
                .mapToDouble(attempt -> attempt.points())
                .sum();
    }
}
