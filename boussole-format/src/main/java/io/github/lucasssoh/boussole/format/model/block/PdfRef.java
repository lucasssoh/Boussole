package io.github.lucasssoh.boussole.format.model.block;

import io.github.lucasssoh.boussole.format.diagnostic.Position;
import io.github.lucasssoh.boussole.format.model.SourceRef;

/** {@code ::pdf[label]{src=… page=…}}: a source document opened beside the text. */
public record PdfRef(String label, SourceRef source) implements Block {

    @Override
    public Position position() {
        return source.position();
    }
}
