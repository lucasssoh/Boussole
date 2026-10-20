package io.github.lucasssoh.boussole.format.model.block;

import io.github.lucasssoh.boussole.format.diagnostic.Position;
import java.util.Optional;

/** {@code :term[text]{id=…}}: a hover definition. Without {@code id}, the text names the term. */
public record TermMention(String text, Optional<String> id, Position position) implements InlineDirective {
}
