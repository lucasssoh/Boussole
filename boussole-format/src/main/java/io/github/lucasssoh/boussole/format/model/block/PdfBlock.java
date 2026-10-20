package io.github.lucasssoh.boussole.format.model.block;

import io.github.lucasssoh.boussole.format.diagnostic.SourcePosition;
import io.github.lucasssoh.boussole.format.model.SourceReference;

/** {@code ::pdf[label]{src=… page=…}}: a source document opened beside the text. */
public record PdfBlock(String label, SourceReference source) implements Block {

    @Override
    public SourcePosition position() {
        return source.position();
    }
}
