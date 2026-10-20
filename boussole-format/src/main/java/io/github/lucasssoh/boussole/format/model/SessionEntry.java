package io.github.lucasssoh.boussole.format.model;

import io.github.lucasssoh.boussole.format.diagnostic.Position;

/**
 * A session as the syllabus plans it; its content is in {@code file}, once written. For a lesson,
 * {@code duration} is the content timed at a normal pace; for a timed session, the time allowed.
 */
public record SessionEntry(
        String id, String title, SessionKind kind, int duration, String goal, String file, Position position) {
}
