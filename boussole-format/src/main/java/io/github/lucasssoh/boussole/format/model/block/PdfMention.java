package io.github.lucasssoh.boussole.format.model.block;

import io.github.lucasssoh.boussole.format.diagnostic.Position;
import io.github.lucasssoh.boussole.format.model.SourceRef;

/** {@code :pdf[label]{src=… page=…}} inside a sentence. */
public record PdfMention(String label, SourceRef source) implements InlineDirective {

    @Override
    public Position position() {
        return source.position();
    }
}
