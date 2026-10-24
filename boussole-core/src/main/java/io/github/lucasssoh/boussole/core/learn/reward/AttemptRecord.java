package io.github.lucasssoh.boussole.core.learn.reward;

import io.github.lucasssoh.boussole.core.learn.check.Verdict;
import java.time.Instant;
import java.util.Optional;

/** One attempt of the history: where it was given, how it was judged, and the points it earned. */
public record AttemptRecord(String subject, String exercise, Optional<String> session, Instant at, Verdict verdict, double points) {
}
