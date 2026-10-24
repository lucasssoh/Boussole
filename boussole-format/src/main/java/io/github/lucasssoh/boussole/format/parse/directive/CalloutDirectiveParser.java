package io.github.lucasssoh.boussole.format.parse.directive;

import io.github.lucasssoh.boussole.format.model.block.Block;
import io.github.lucasssoh.boussole.format.model.block.CalloutBlock;
import io.github.lucasssoh.boussole.format.model.block.CalloutKind;
import io.github.lucasssoh.boussole.format.parse.ParseContext;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/** {@code :::note}, {@code :::tip}, {@code :::warning} and {@code :::beyond}, one instance per kind. */
final class CalloutDirectiveParser implements DirectiveParser {

    private final CalloutKind kind;
    private final DirectiveSpec spec;

    CalloutDirectiveParser(CalloutKind kind) {
        this.kind = kind;
        this.spec = new DirectiveSpec(
                kind.name().toLowerCase(Locale.ROOT),
                DirectiveKind.CONTAINER,
                Set.of(),
                Set.of(),
                DirectivePlaces.union(DirectivePlaces.FLOW, Set.of("example", "recall", "hint", "solution")));
    }

    @Override
    public DirectiveSpec spec() {
        return spec;
    }

    @Override
    public Optional<Block> parse(CheckedDirective directive, ParseContext context) {
        return Optional.of(new CalloutBlock(kind, context.blocks(directive.children(), spec.name()), context.position(directive.line())));
    }
}
