package io.github.lucasssoh.boussole.core.learn;

import io.github.lucasssoh.boussole.core.learn.progress.LastActivity;
import io.github.lucasssoh.boussole.core.learn.progress.ProgressRepository;
import io.github.lucasssoh.boussole.format.model.ChapterLinks;
import io.github.lucasssoh.boussole.format.model.Course;
import io.github.lucasssoh.boussole.core.learn.progress.ExerciseProgress;
import io.github.lucasssoh.boussole.core.learn.progress.SessionProgress;
import io.github.lucasssoh.boussole.format.model.CourseSession;
import io.github.lucasssoh.boussole.format.model.SessionId;
import io.github.lucasssoh.boussole.core.learn.reward.TrackScores;
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
        Map<String, List<SessionId>> waiting = waitingFor(course, finished);
        ChapterLinks links = ChapterLinks.of(course);
        Map<String, List<String>> chapterWaiting = chapterWaitingFor(course, links, finished);
        return new CourseOverview(course, languages, finished, sessionProgress(course, progress.exercises(subject)),
                last.map(LastActivity::at), waiting, links.direct(), chapterWaiting, TrackScores.of(course, progress.attemptHistory()),
                resume(course, finished, waiting, chapterWaiting, last));
    }

    /**
     * The chapters still waiting for others: a chapter is finished when every written session of it
     * is, and one waits for each chapter it needs ({@link ChapterLinks}) not finished yet.
     */
    private static Map<String, List<String>> chapterWaitingFor(Course course, ChapterLinks links, Set<String> finished) {
        Set<String> done = course.syllabus().chapters().stream()
                .filter(chapter -> course.sessions().stream()
                        .filter(session -> session.chapter().id().equals(chapter.id()))
                        .allMatch(session -> finished.contains(session.entry().id())))
                .map(chapter -> chapter.id())
                .collect(Collectors.toSet());
        Map<String, List<String>> waiting = new HashMap<>();
        for (var chapter : course.syllabus().chapters()) {
            List<String> missing = links.after(chapter.id()).stream()
                    .filter(before -> !done.contains(before))
                    .sorted()
                    .toList();
            if (!missing.isEmpty()) {
                waiting.put(chapter.id(), missing);
            }
        }
        return waiting;
    }

    /** The written sessions whose prerequisites are not all finished, with those not finished. */
    private Map<String, List<SessionId>> waitingFor(Course course, Set<String> finished) {
        String subject = course.subject().id();
        Map<String, Set<String>> finishedBySubject = new HashMap<>(Map.of(subject, finished));
        Map<String, List<SessionId>> waiting = new HashMap<>();
        for (CourseSession session : course.sessions()) {
            List<SessionId> missing = session.session().header().prerequisites().stream()
                    .flatMap(id -> fullId(id).stream())
                    .filter(id -> !finishedBySubject.computeIfAbsent(id.subject(), progress::finishedSessions).contains(id.session()))
                    .toList();
            if (!missing.isEmpty()) {
                waiting.put(session.entry().id(), missing);
            }
        }
        return waiting;
    }

    /** {@code <subject>.<chapter>.<session>}; the validator reports any other form ({@code F030}). */
    private static Optional<SessionId> fullId(String id) {
        int session = id.lastIndexOf('.');
        int chapter = session < 0 ? -1 : id.lastIndexOf('.', session - 1);
        return chapter <= 0 ? Optional.empty()
                : Optional.of(new SessionId(id.substring(0, chapter), id.substring(chapter + 1, session), id.substring(session + 1)));
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

    /**
     * The session of the last answer while it is not finished, else the first written session not
     * finished whose prerequisites are and whose chapter waits for no other, else the first not finished.
     */
    private static Optional<String> resume(Course course, Set<String> finished, Map<String, List<SessionId>> waiting,
            Map<String, List<String>> chapterWaiting, Optional<LastActivity> last) {
        Set<String> written = course.sessions().stream().map(session -> session.entry().id()).collect(Collectors.toSet());
        Optional<String> current = last.filter(activity -> !activity.finished())
                .flatMap(LastActivity::session)
                .filter(session -> written.contains(session) && !finished.contains(session));
        Set<String> waitingChapters = course.sessions().stream()
                .filter(session -> chapterWaiting.containsKey(session.chapter().id()))
                .map(session -> session.entry().id())
                .collect(Collectors.toSet());
        List<String> unfinished = course.sessions().stream()
                .map(session -> session.entry().id())
                .filter(session -> !finished.contains(session))
                .toList();
        return current.or(() -> unfinished.stream()
                        .filter(session -> !waiting.containsKey(session) && !waitingChapters.contains(session))
                        .findFirst())
                .or(() -> unfinished.stream().findFirst());
    }
}
