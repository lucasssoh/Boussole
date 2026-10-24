package io.github.lucasssoh.boussole.core.learn.reward;

import java.time.Instant;

/** One session finished, in the history, with the bonus it earned when done without a mistake. */
public record SessionFinishedRecord(String subject, String session, Instant at, double bonus) {
}
