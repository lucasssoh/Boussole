package io.github.lucasssoh.boussole.format.model;

import io.github.lucasssoh.boussole.format.diagnostic.Position;
import java.util.List;

/** An entry of the lexicon. */
public record Term(
        String id, String term, List<String> aliases, String definition, List<String> see, Position position) {
}
