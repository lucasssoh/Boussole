package io.github.lucasssoh.boussole.format.model.block;

import io.github.lucasssoh.boussole.format.diagnostic.SourcePosition;

/** A web page cited by the course, opened beside the text: documentation, an article, a reference. */
public record LinkBlock(String label, String url, SourcePosition position) implements Block {
}
