package io.github.lucasssoh.boussole.format.parse.directive;

import io.github.lucasssoh.boussole.format.model.block.Block;
import io.github.lucasssoh.boussole.format.model.block.Callout;
import io.github.lucasssoh.boussole.format.model.block.CalloutKind;
import io.github.lucasssoh.boussole.format.parse.ParseContext;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/** {@code :::note}, {@code :::tip} and {@code :::warning}, one instance per kind. */
final class CalloutParser implements DirectiveParser {

    private final CalloutKind kind;
    private final DirectiveSpec spec;

    CalloutParser(CalloutKind kind) {
        this.kind = kind;
        this.spec = new DirectiveSpec(
                kind.name().toLowerCase(Locale.ROOT),
                DirectiveKind.CONTAINER,
                Set.of(),
                Set.of(),
                Parents.union(Parents.FLOW, Set.of("example", "recall", "hint", "solution")));
    }

    @Override
    public DirectiveSpec spec() {
        return spec;
    }

    @Override
    public Optional<Block> parse(Directive directive, ParseContext context) {
        return Optional.of(new Callout(kind, context.blocks(directive.children(), spec.name()), context.position(directive.line())));
    }
}
