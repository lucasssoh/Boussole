package io.github.lucasssoh.boussole.format.model;

import java.util.List;

/** The plan of a course: its chapters and sessions, in order. */
public record CourseSyllabus(SyllabusStatus status, List<SyllabusChapter> chapters) {

    public static CourseSyllabus empty() {
        return new CourseSyllabus(SyllabusStatus.PROPOSED, List.of());
    }
}
