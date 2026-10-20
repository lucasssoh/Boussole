package io.github.lucasssoh.boussole.core.learn;

import io.github.lucasssoh.boussole.format.model.Course;
import io.github.lucasssoh.boussole.core.learn.progress.SessionProgress;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * A course, the sessions the learner has finished in it, how far they are in each written session
 * (by session ID), when they last studied it, and the session to resume: the one they were in, or
 * else the next one not finished.
 */
public record CourseOverview(
        Course course,
        Set<String> finishedSessions,
        Map<String, SessionProgress> sessionProgress,
        Optional<Instant> lastStudied,
        Optional<String> resume) {

    public CourseOverview {
        sessionProgress = Map.copyOf(sessionProgress);
    }
}
