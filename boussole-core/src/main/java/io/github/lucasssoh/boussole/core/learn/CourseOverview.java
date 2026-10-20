package io.github.lucasssoh.boussole.core.learn;

import io.github.lucasssoh.boussole.format.model.Course;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;

/**
 * A course, the sessions the learner has finished in it, when they last studied it, and the
 * session to resume: the one they were in, or else the next one not finished.
 */
public record CourseOverview(Course course, Set<String> finishedSessions, Optional<Instant> lastStudied, Optional<String> resume) {
}
