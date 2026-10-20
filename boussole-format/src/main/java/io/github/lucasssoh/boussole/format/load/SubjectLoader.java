package io.github.lucasssoh.boussole.format.load;

import io.github.lucasssoh.boussole.format.diagnostic.Diagnostics;
import io.github.lucasssoh.boussole.format.model.Chapter;
import io.github.lucasssoh.boussole.format.model.Course;
import io.github.lucasssoh.boussole.format.model.LearnerProfile;
import io.github.lucasssoh.boussole.format.model.Lexicon;
import io.github.lucasssoh.boussole.format.model.LoadedSession;
import io.github.lucasssoh.boussole.format.model.SessionEntry;
import io.github.lucasssoh.boussole.format.model.Subject;
import io.github.lucasssoh.boussole.format.model.Syllabus;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Reads a subject folder into a {@link Course}, reporting what it cannot read. Only a missing or
 * unreadable {@code subject.yaml} leaves nothing to read.
 */
public final class SubjectLoader {

    private SubjectLoader() {
    }

    public static Optional<Course> load(Path root, Diagnostics diagnostics) {
        SubjectFiles files = new SubjectFiles(root, diagnostics);
        Optional<Subject> subject = SubjectReader.read(files, diagnostics);
        if (subject.isEmpty()) {
            return Optional.empty();
        }
        Syllabus syllabus = SyllabusReader.read(files, diagnostics);
        Lexicon lexicon = LexiconReader.read(files, diagnostics);
        Optional<LearnerProfile> learner = LearnerProfileReader.read(files, diagnostics);
        Course withoutSessions = new Course(root, subject.get(), syllabus, lexicon, learner, List.of());
        return Optional.of(new Course(
                root, subject.get(), syllabus, lexicon, learner, sessions(withoutSessions, files, diagnostics)));
    }

    private static List<LoadedSession> sessions(Course course, SubjectFiles files, Diagnostics diagnostics) {
        List<LoadedSession> sessions = new ArrayList<>();
        for (Chapter chapter : course.syllabus().chapters()) {
            for (SessionEntry entry : chapter.sessions()) {
                String file = "cursus/" + chapter.dir() + "/" + entry.file();
                if (!chapter.dir().isEmpty() && !entry.file().isEmpty() && files.exists(file)) {
                    files.read(file).ifPresent(text -> sessions.add(new LoadedSession(
                            course.sessionId(chapter, entry), chapter, entry, file, SessionReader.read(text, file, diagnostics))));
                }
            }
        }
        return List.copyOf(sessions);
    }
}
