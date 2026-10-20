package io.github.lucasssoh.boussole.core.learn.reward;

import io.github.lucasssoh.boussole.core.learn.check.Verdict;
import java.time.Instant;

/** One attempt of the history. */
public record AttemptRecord(String subject, String exercise, Instant at, Verdict verdict) {
}
