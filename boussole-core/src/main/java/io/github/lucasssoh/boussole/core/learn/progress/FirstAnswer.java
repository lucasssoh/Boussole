package io.github.lucasssoh.boussole.core.learn.progress;

import io.github.lucasssoh.boussole.core.learn.check.Verdict;

/** The first answer to a question in a session: its verdict and its stars, out of 3. */
public record FirstAnswer(Verdict verdict, int stars) {
}
