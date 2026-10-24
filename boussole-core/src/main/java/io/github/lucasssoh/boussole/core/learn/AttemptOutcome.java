package io.github.lucasssoh.boussole.core.learn;

import io.github.lucasssoh.boussole.core.learn.check.CheckResult;
import io.github.lucasssoh.boussole.core.learn.progress.ExerciseProgress;
import io.github.lucasssoh.boussole.core.learn.reward.QuestionScore;

/**
 * How an attempt was judged, the progress on the exercise after it, what the answer was worth
 * ({@code score}, before the occasion and the level of the track) and the points it earned.
 */
public record AttemptOutcome(CheckResult result, ExerciseProgress progress, QuestionScore score, double pointsGained) {
}
