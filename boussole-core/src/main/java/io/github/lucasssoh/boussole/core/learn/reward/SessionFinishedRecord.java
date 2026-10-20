package io.github.lucasssoh.boussole.core.learn.reward;

import java.time.Instant;

/** One session finished, in the history. */
public record SessionFinishedRecord(String subject, Instant at) {
}
