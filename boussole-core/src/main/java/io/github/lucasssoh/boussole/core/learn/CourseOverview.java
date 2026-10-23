package io.github.lucasssoh.boussole.core.learn;

import io.github.lucasssoh.boussole.format.model.Course;
import io.github.lucasssoh.boussole.core.learn.progress.SessionProgress;
import io.github.lucasssoh.boussole.format.model.SessionId;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * A course in the language it is followed in, the languages it exists in, the sessions the learner
 * has finished in it, how far they are in each written session (by session ID), when they last
 * studied it, the written sessions still waiting for prerequisites not finished (by session ID,
 * with those prerequisites), and the session to resume: the one they were in, or else the next one
 * not finished whose prerequisites are.
 */
public record CourseOverview(
        Course course,
        List<String> languages,
        Set<String> finishedSessions,
        Map<String, SessionProgress> sessionProgress,
        Optional<Instant> lastStudied,
        Map<String, List<SessionId>> waitingFor,
        Optional<String> resume) {

    public CourseOverview {
        languages = List.copyOf(languages);
        sessionProgress = Map.copyOf(sessionProgress);
        waitingFor = Map.copyOf(waitingFor);
    }
}
