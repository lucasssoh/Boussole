package io.github.lucasssoh.boussole.format.parse.directive;

import io.github.lucasssoh.boussole.format.model.block.Block;
import io.github.lucasssoh.boussole.format.model.block.Recall;
import io.github.lucasssoh.boussole.format.parse.ParseContext;
import java.util.Optional;
import java.util.Set;

/** {@code :::recall}: a reminder just before it is needed. */
final class RecallParser implements DirectiveParser {

    private static final DirectiveSpec SPEC = new DirectiveSpec(
            "recall", DirectiveKind.CONTAINER, Set.of(), Set.of(), Parents.union(Parents.FLOW, Set.of("hint", "solution")));

    @Override
    public DirectiveSpec spec() {
        return SPEC;
    }

    @Override
    public Optional<Block> parse(Directive directive, ParseContext context) {
        return Optional.of(new Recall(context.blocks(directive.children(), SPEC.name()), context.position(directive.line())));
    }
}
