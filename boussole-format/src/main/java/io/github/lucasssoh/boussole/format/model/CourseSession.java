package io.github.lucasssoh.boussole.format.model;

/** A session of the syllabus whose file was found and read. */
public record CourseSession(SessionId id, SyllabusChapter chapter, SyllabusSession entry, String file, SessionContent session) {
}
