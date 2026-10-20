package io.github.lucasssoh.boussole.server.block;

import io.github.lucasssoh.boussole.core.learn.progress.ExerciseProgress;
import java.util.Locale;

/** The learner's progress on a question; {@code lastVerdict} is null before any attempt. */
public record ProgressView(int attempts, boolean solved, String lastVerdict) {

    public static ProgressView of(ExerciseProgress progress) {
        return new ProgressView(
                progress.attempts(),
                progress.solved(),
                progress.lastVerdict().map(verdict -> verdict.name().toLowerCase(Locale.ROOT)).orElse(null));
    }
}
