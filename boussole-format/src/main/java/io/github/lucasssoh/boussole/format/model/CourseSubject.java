package io.github.lucasssoh.boussole.format.model;

import io.github.lucasssoh.boussole.format.diagnostic.SourcePosition;
import java.util.Optional;

/** What {@code subject.yaml} says about a subject. */
public record CourseSubject(
        String id, String name, String lang, String sourcesDir, Optional<String> description, SourcePosition position) {
}
