package io.github.lucasssoh.boussole.format.parse.directive;

import io.github.lucasssoh.boussole.format.model.block.Block;
import io.github.lucasssoh.boussole.format.model.block.ExerciseRef;
import io.github.lucasssoh.boussole.format.parse.ParseContext;
import java.util.Optional;
import java.util.Set;

/** {@code ::ref{id=…}}: an exercise of the subject asked again, in a section or a review. */
final class RefParser implements DirectiveParser {

    private static final DirectiveSpec SPEC =
            new DirectiveSpec("ref", DirectiveKind.LEAF, Set.of("id"), Set.of(), Parents.FLOW);

    @Override
    public DirectiveSpec spec() {
        return SPEC;
    }

    @Override
    public Optional<Block> parse(Directive directive, ParseContext context) {
        return Optional.of(new ExerciseRef(directive.attributes().required("id"), context.position(directive.line())));
    }
}
