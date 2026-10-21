package io.github.lucasssoh.boussole.format.model.block;

import io.github.lucasssoh.boussole.format.diagnostic.SourcePosition;

/** {@code :link[text]{url=…}} inside a sentence: a web page cited, opened beside the text. */
public record LinkInlineDirective(String label, String url, SourcePosition position) implements InlineDirective {
}
