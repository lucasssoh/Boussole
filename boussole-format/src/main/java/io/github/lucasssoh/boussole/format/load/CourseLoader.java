package io.github.lucasssoh.boussole.format.load;

import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCollector;
import io.github.lucasssoh.boussole.format.model.SyllabusChapter;
import io.github.lucasssoh.boussole.format.model.Course;
import io.github.lucasssoh.boussole.format.model.LearnerProfile;
import io.github.lucasssoh.boussole.format.model.CourseLexicon;
import io.github.lucasssoh.boussole.format.model.CourseSession;
import io.github.lucasssoh.boussole.format.model.CourseSources;
import io.github.lucasssoh.boussole.format.model.SyllabusSession;
import io.github.lucasssoh.boussole.format.model.CourseSubject;
import io.github.lucasssoh.boussole.format.model.CourseSyllabus;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Reads a subject folder into a {@link Course}, reporting what it cannot read. Only a missing or
 * unreadable {@code subject.yaml} leaves nothing to read.
 */
public final class CourseLoader {

    private CourseLoader() {
    }

    public static Optional<Course> load(Path root, DiagnosticCollector diagnostics) {
        SubjectFolderFiles files = new SubjectFolderFiles(root, diagnostics);
        Optional<CourseSubject> subject = CourseSubjectReader.read(files, diagnostics);
        if (subject.isEmpty()) {
            return Optional.empty();
        }
        CourseSyllabus syllabus = CourseSyllabusReader.read(files, diagnostics);
        CourseLexicon lexicon = CourseLexiconReader.read(files, diagnostics);
        Optional<LearnerProfile> learner = LearnerProfileReader.read(files, diagnostics);
        Optional<CourseSources> sources = CourseSourcesReader.read(files, diagnostics);
        Course withoutSessions = new Course(root, subject.get(), syllabus, lexicon, learner, sources, List.of());
        return Optional.of(new Course(
                root, subject.get(), syllabus, lexicon, learner, sources, sessions(withoutSessions, files, diagnostics)));
    }

    private static List<CourseSession> sessions(Course course, SubjectFolderFiles files, DiagnosticCollector diagnostics) {
        List<CourseSession> sessions = new ArrayList<>();
        for (SyllabusChapter chapter : course.syllabus().chapters()) {
            for (SyllabusSession entry : chapter.sessions()) {
                String file = "cursus/" + chapter.dir() + "/" + entry.file();
                if (!chapter.dir().isEmpty() && !entry.file().isEmpty() && files.exists(file)) {
                    files.read(file).ifPresent(text -> sessions.add(new CourseSession(
                            course.sessionId(chapter, entry), chapter, entry, file, SessionFileReader.read(text, file, diagnostics))));
                }
            }
        }
        return List.copyOf(sessions);
    }
}
