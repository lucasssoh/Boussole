package io.github.lucasssoh.boussole.core.learn;

import io.github.lucasssoh.boussole.core.learn.check.CheckResult;
import io.github.lucasssoh.boussole.core.learn.progress.ExerciseProgress;

/** How an attempt was judged, the progress on the exercise after it, and the experience it earned. */
public record AttemptOutcome(CheckResult result, ExerciseProgress progress, int xpGained) {
}
