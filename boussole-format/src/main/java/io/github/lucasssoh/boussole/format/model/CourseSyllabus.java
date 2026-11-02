package io.github.lucasssoh.boussole.format.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * The plan of a course: its chapters and sessions, in order. {@code locked} is the day the learner
 * said every document is in: documents that arrive after are left out, and the final mock exam can
 * be written.
 */
public record CourseSyllabus(SyllabusStatus status, Optional<LocalDate> locked, List<SyllabusChapter> chapters) {

    public CourseSyllabus(SyllabusStatus status, List<SyllabusChapter> chapters) {
        this(status, Optional.empty(), chapters);
    }

    public static CourseSyllabus empty() {
        return new CourseSyllabus(SyllabusStatus.PROPOSED, List.of());
    }

    public boolean isLocked() {
        return locked.isPresent();
    }

    /** The final mock exam: the last session of the plan, when it is an exam. It waits for the lock. */
    public Optional<SyllabusSession> finalExam() {
        return chapters.isEmpty() || chapters.getLast().sessions().isEmpty() ? Optional.empty()
                : Optional.of(chapters.getLast().sessions().getLast()).filter(session -> session.kind() == SessionKind.EXAM);
    }
}
