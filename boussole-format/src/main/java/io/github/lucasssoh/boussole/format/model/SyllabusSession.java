package io.github.lucasssoh.boussole.format.model;

import java.util.List;

import io.github.lucasssoh.boussole.format.diagnostic.SourcePosition;

/**
 * A session as the syllabus plans it; its content is in {@code file}, once written. For a lesson,
 * {@code duration} is the content timed at a normal pace; for a timed session, the time allowed.
 * {@code prerequisites} are the full session IDs the plan decided it needs, which its front matter
 * repeats once written.
 */
public record SyllabusSession(
        String id, String title, SessionKind kind, int duration, String goal, String file, List<String> prerequisites,
        SourcePosition position) {

    public SyllabusSession {
        prerequisites = List.copyOf(prerequisites);
    }
}
