package io.github.lucasssoh.boussole.format.model.block;

import io.github.lucasssoh.boussole.format.diagnostic.Position;

/** A directive inside a sentence: {@code :name[text]{attributes}}. */
public sealed interface InlineDirective permits TermMention, PdfMention {

    Position position();
}
