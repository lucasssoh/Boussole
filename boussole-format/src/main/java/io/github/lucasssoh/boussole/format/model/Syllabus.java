package io.github.lucasssoh.boussole.format.model;

import java.util.List;

/** The plan of a course: its chapters and sessions, in order. */
public record Syllabus(SyllabusStatus status, List<Chapter> chapters) {

    public static Syllabus empty() {
        return new Syllabus(SyllabusStatus.PROPOSED, List.of());
    }
}
