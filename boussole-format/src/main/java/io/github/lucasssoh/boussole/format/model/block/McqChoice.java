package io.github.lucasssoh.boussole.format.model.block;

import io.github.lucasssoh.boussole.format.diagnostic.SourcePosition;
import java.util.Optional;

/** A choice of a mcq, with the feedback shown when the learner picks it. */
public record McqChoice(String text, boolean correct, Optional<String> feedback, SourcePosition position) {
}
