package io.github.lucasssoh.boussole.format.model.block;

import io.github.lucasssoh.boussole.format.diagnostic.SourcePosition;
import io.github.lucasssoh.boussole.format.model.SourceReference;

/** {@code :pdf[label]{src=… page=…}} inside a sentence. */
public record PdfInlineDirective(String label, SourceReference source) implements InlineDirective {

    @Override
    public SourcePosition position() {
        return source.position();
    }
}
