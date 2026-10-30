package io.github.lucasssoh.boussole.server.block;

import io.github.lucasssoh.boussole.core.learn.check.Verdict;
import io.github.lucasssoh.boussole.core.learn.progress.ExerciseProgress;
import io.github.lucasssoh.boussole.core.learn.progress.FirstAnswer;
import java.util.Locale;
import java.util.Optional;

/**
 * The learner's progress on a question; {@code lastVerdict} is null before any attempt, and
 * {@code answeredHere} is the verdict of the answer already given in this lesson, which is final,
 * with its {@code answeredStars}, out of 3.
 */
public record ProgressView(int attempts, boolean solved, String lastVerdict, String answeredHere, Integer answeredStars) {

    /** The progress outside an opened session, as after an attempt: the interface knows it has just answered. */
    public static ProgressView of(ExerciseProgress progress) {
        return of(progress, Optional.empty());
    }

    public static ProgressView of(ExerciseProgress progress, Optional<FirstAnswer> answeredHere) {
        return new ProgressView(
                progress.attempts(),
                progress.solved(),
                progress.lastVerdict().map(ProgressView::name).orElse(null),
                answeredHere.map(answer -> name(answer.verdict())).orElse(null),
                answeredHere.map(FirstAnswer::stars).orElse(null));
    }

    private static String name(Verdict verdict) {
        return verdict.name().toLowerCase(Locale.ROOT);
    }
}
