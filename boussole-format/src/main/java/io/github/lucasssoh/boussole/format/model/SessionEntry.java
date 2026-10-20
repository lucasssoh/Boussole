package io.github.lucasssoh.boussole.format.model;

import io.github.lucasssoh.boussole.format.diagnostic.Position;

/** A session as the syllabus plans it; its content is in {@code file}, once written. */
public record SessionEntry(String id, String title, int duration, String goal, String file, Position position) {
}
