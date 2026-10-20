package io.github.lucasssoh.boussole.format.parse.directive;

import io.github.lucasssoh.boussole.format.model.block.Block;
import io.github.lucasssoh.boussole.format.parse.ParseContext;
import java.util.Optional;

/**
 * Builds the block of one directive. Implementations may throw
 * {@link io.github.lucasssoh.boussole.format.parse.InvalidAttributeException} for an attribute
 * value they cannot use, and return empty after reporting any other problem.
 */
public interface DirectiveParser {

    DirectiveSpec spec();

    Optional<Block> parse(Directive directive, ParseContext context);
}
