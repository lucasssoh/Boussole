package io.github.lucasssoh.boussole.format.model;

import io.github.lucasssoh.boussole.format.diagnostic.Position;
import java.util.Optional;

/** What {@code subject.yaml} says about a subject. */
public record Subject(
        String id, String name, String lang, String sourcesDir, Optional<String> description, Position position) {
}
