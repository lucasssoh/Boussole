package io.github.lucasssoh.boussole.format.parse.directive;

import io.github.lucasssoh.boussole.format.model.block.Block;
import io.github.lucasssoh.boussole.format.model.block.Example;
import io.github.lucasssoh.boussole.format.parse.ParseContext;
import java.util.Optional;
import java.util.Set;

/** {@code :::example[title]}: a worked example. */
final class ExampleParser implements DirectiveParser {

    private static final DirectiveSpec SPEC =
            new DirectiveSpec("example", DirectiveKind.CONTAINER, Set.of(), Set.of(), Parents.FLOW);

    @Override
    public DirectiveSpec spec() {
        return SPEC;
    }

    @Override
    public Optional<Block> parse(Directive directive, ParseContext context) {
        return Optional.of(new Example(
                directive.label(), context.blocks(directive.children(), SPEC.name()), context.position(directive.line())));
    }
}
