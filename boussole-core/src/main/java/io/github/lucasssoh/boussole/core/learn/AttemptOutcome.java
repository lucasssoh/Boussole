package io.github.lucasssoh.boussole.core.learn;

import io.github.lucasssoh.boussole.core.learn.check.CheckResult;
import io.github.lucasssoh.boussole.core.learn.progress.ExerciseProgress;

/** How an attempt was judged, and the progress on the exercise after it. */
public record AttemptOutcome(CheckResult result, ExerciseProgress progress) {
}
