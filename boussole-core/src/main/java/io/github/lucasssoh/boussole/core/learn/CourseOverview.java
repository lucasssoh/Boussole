package io.github.lucasssoh.boussole.core.learn;

import io.github.lucasssoh.boussole.format.model.ChapterLinks;
import io.github.lucasssoh.boussole.format.model.Course;
import io.github.lucasssoh.boussole.core.learn.progress.SessionProgress;
import io.github.lucasssoh.boussole.format.model.SessionId;
import io.github.lucasssoh.boussole.core.learn.reward.TrackScore;
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
 * not finished whose prerequisites are; its score: points earned, missed, out of all it gives.
 * Chapters follow one another ({@link ChapterLinks}): {@code chapterLinks} gives, for each chapter,
 * the chapters it waits for once those another path gives are left out (what a map draws), and
 * {@code chapterWaitingFor} those of them it still waits for, not finished.
 */
public record CourseOverview(
        Course course,
        List<String> languages,
        Set<String> finishedSessions,
        Map<String, SessionProgress> sessionProgress,
        Optional<Instant> lastStudied,
        Map<String, List<SessionId>> waitingFor,
        Map<String, Set<String>> chapterLinks,
        Map<String, List<String>> chapterWaitingFor,
        TrackScore score,
        Optional<String> resume) {

    public CourseOverview {
        languages = List.copyOf(languages);
        sessionProgress = Map.copyOf(sessionProgress);
        waitingFor = Map.copyOf(waitingFor);
        chapterLinks = Map.copyOf(chapterLinks);
        chapterWaitingFor = Map.copyOf(chapterWaitingFor);
    }
}
