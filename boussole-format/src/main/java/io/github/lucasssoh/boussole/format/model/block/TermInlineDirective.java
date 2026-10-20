package io.github.lucasssoh.boussole.format.model.block;

import io.github.lucasssoh.boussole.format.diagnostic.SourcePosition;
import java.util.Optional;

/** {@code :term[text]{id=…}}: a hover definition. Without {@code id}, the text names the term. */
public record TermInlineDirective(String text, Optional<String> id, SourcePosition position) implements InlineDirective {
}
