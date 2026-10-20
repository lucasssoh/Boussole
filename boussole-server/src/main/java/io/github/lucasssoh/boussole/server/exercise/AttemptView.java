package io.github.lucasssoh.boussole.server.exercise;

import io.github.lucasssoh.boussole.server.block.ProgressView;
import io.github.lucasssoh.boussole.core.learn.AttemptOutcome;
import java.util.List;
import java.util.Locale;

/**
 * How an attempt was judged: the verdict, each part of the response, the progress after it, and
 * the correction, which the learner sees right after answering.
 */
record AttemptView(String verdict, List<ItemView> items, ProgressView progress, SolutionView solution) {

    static AttemptView of(AttemptOutcome outcome, SolutionView solution) {
        return new AttemptView(
                outcome.result().verdict().name().toLowerCase(Locale.ROOT),
                outcome.result().items().stream()
                        .map(item -> new ItemView(item.key(), item.correct(), item.feedback().orElse(null)))
                        .toList(),
                ProgressView.of(outcome.progress()),
                solution);
    }

    record ItemView(String key, boolean correct, String feedback) {
    }
}
