package io.github.lucasssoh.boussole.core.learn;

import io.github.lucasssoh.boussole.core.learn.progress.LastActivity;
import io.github.lucasssoh.boussole.core.learn.progress.ProgressRepository;
import io.github.lucasssoh.boussole.format.model.Course;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/** Builds the overview of a course from the learner's progress. */
final class CourseOverviews {

    private final ProgressRepository progress;

    CourseOverviews(ProgressRepository progress) {
        this.progress = progress;
    }

    CourseOverview of(Course course) {
        String subject = course.subject().id();
        Set<String> finished = progress.finishedSessions(subject);
        Optional<LastActivity> last = progress.lastActivity(subject);
        return new CourseOverview(course, finished, last.map(LastActivity::at), resume(course, finished, last));
    }

    /** The session of the last answer while it is not finished, else the first written session not finished. */
    private static Optional<String> resume(Course course, Set<String> finished, Optional<LastActivity> last) {
        Set<String> written = course.sessions().stream().map(session -> session.entry().id()).collect(Collectors.toSet());
        Optional<String> current = last.filter(activity -> !activity.finished())
                .flatMap(LastActivity::session)
                .filter(session -> written.contains(session) && !finished.contains(session));
        return current.or(() -> course.sessions().stream()
                .map(session -> session.entry().id())
                .filter(session -> !finished.contains(session))
                .findFirst());
    }
}
