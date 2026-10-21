package io.github.lucasssoh.boussole.core.learn;

import io.github.lucasssoh.boussole.core.learn.progress.LastActivity;
import io.github.lucasssoh.boussole.core.learn.progress.ProgressRepository;
import io.github.lucasssoh.boussole.format.model.Course;
import io.github.lucasssoh.boussole.core.learn.progress.ExerciseProgress;
import io.github.lucasssoh.boussole.core.learn.progress.SessionProgress;
import io.github.lucasssoh.boussole.format.model.CourseSession;
import io.github.lucasssoh.boussole.format.model.block.ExerciseRefBlock;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Builds the overview of a course from the learner's progress. */
final class CourseOverviews {

    private final ProgressRepository progress;

    CourseOverviews(ProgressRepository progress) {
        this.progress = progress;
    }

    CourseOverview of(Course course, List<String> languages) {
        String subject = course.subject().id();
        Set<String> finished = progress.finishedSessions(subject);
        Optional<LastActivity> last = progress.lastActivity(subject);
        return new CourseOverview(course, languages, finished, sessionProgress(course, progress.exercises(subject)),
                last.map(LastActivity::at), resume(course, finished, last));
    }

    private static Map<String, SessionProgress> sessionProgress(Course course, Map<String, ExerciseProgress> exercises) {
        Map<String, SessionProgress> found = new HashMap<>();
        for (CourseSession session : course.sessions()) {
            List<String> questions = session.session().blocks()
                    .flatMap(block -> block instanceof ExerciseRefBlock ref ? Stream.of(ref.id()) : CourseQuestions.id(block).stream())
                    .distinct()
                    .toList();
            List<ExerciseProgress> done = questions.stream()
                    .map(id -> exercises.getOrDefault(id, ExerciseProgress.NONE))
                    .filter(progress -> progress.attempts() > 0)
                    .toList();
            found.put(session.entry().id(), new SessionProgress(questions.size(), done.size(),
                    (int) done.stream().filter(ExerciseProgress::solved).count()));
        }
        return found;
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
