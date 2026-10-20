package io.github.lucasssoh.boussole.format.model;

import io.github.lucasssoh.boussole.format.diagnostic.SourcePosition;
import java.util.List;

/** An entry of the lexicon. */
public record LexiconTerm(
        String id, String term, List<String> aliases, String definition, List<String> see, SourcePosition position) {
}
