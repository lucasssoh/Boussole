package io.github.lucasssoh.boussole.format.model;

import java.util.List;

/** {@code cursus/sources.yaml}: the documents the course was built from, and what it did with each. */
public record CourseSources(List<SourceEntry> entries) {
}
