package io.github.lucasssoh.boussole.core.learn.progress;

import java.time.Instant;
import java.util.Optional;

/**
 * The last thing the learner did in a subject: an answer given in a session, or a session
 * finished. An answer given outside any session has no session.
 */
public record LastActivity(Instant at, Optional<String> session, boolean finished) {
}
