package io.github.lucasssoh.boussole.format.model;

/** A session of the syllabus whose file was found and read. */
public record LoadedSession(SessionId id, Chapter chapter, SessionEntry entry, String file, Session session) {
}
