package io.github.lucasssoh.boussole.format.parse.directive;

import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCode;
import io.github.lucasssoh.boussole.format.model.block.Block;
import io.github.lucasssoh.boussole.format.model.block.LandmarkBlock;
import io.github.lucasssoh.boussole.format.parse.ParseContext;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** {@code :::landmark[title]{#id}}: what the track keeps coming back to, kept in view beside the steps. */
final class LandmarkDirectiveParser implements DirectiveParser {

    private static final DirectiveSpec SPEC =
            new DirectiveSpec("landmark", DirectiveKind.CONTAINER, Set.of("id"), Set.of(), DirectivePlaces.FLOW);

    @Override
    public DirectiveSpec spec() {
        return SPEC;
    }

    @Override
    public Optional<Block> parse(CheckedDirective directive, ParseContext context) {
        String id = directive.attributes().required("id");
        String title = directive.label() == null ? "" : directive.label().strip();
        List<Block> content = context.blocks(directive.children(), SPEC.name());
        if (title.isEmpty()) {
            context.report(DiagnosticCode.F050, directive.line(),
                    "landmark `" + id + "` has no title: say what it is, `:::landmark[Le programme de l'exemple du cours]{#" + id + "}`");
        }
        if (content.isEmpty()) {
            context.report(DiagnosticCode.F050, directive.line(),
                    "landmark `" + id + "` is empty: put inside it what the learner keeps in view (a program, a table, a graph)");
        }
        return Optional.of(new LandmarkBlock(id, title, content, context.position(directive.line())));
    }
}
