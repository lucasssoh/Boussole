package io.github.lucasssoh.boussole.format.parse.directive;

import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCode;
import io.github.lucasssoh.boussole.format.model.block.Block;
import io.github.lucasssoh.boussole.format.model.block.McqBlock;
import io.github.lucasssoh.boussole.format.parse.ParseContext;
import java.util.Optional;
import java.util.Set;

/** {@code ::::mcq{#id multiple=true}}: a statement ending with a task list, hints and a solution. */
final class McqDirectiveParser implements DirectiveParser {

    private static final DirectiveSpec SPEC =
            new DirectiveSpec("mcq", DirectiveKind.CONTAINER, Set.of("id"), Set.of("multiple", "minutes", "hook"), DirectivePlaces.FLOW);

    @Override
    public DirectiveSpec spec() {
        return SPEC;
    }

    @Override
    public Optional<Block> parse(CheckedDirective directive, ParseContext context) {
        String id = directive.attributes().required("id");
        boolean multiple = directive.attributes().flag("multiple");
        ExerciseParts parts = ExerciseParts.split(directive, context);
        if (!parts.answers().isEmpty()) {
            context.report(DiagnosticCode.F021, parts.answers().getFirst().line(), "an MCQ has no `answer`: tick the right choices");
        }
        McqChoiceReader.Result read = McqChoiceReader.read(parts.statement(), context);
        return Optional.of(new McqBlock(
                id,
                multiple,
                context.blocks(read.statement(), SPEC.name()),
                read.choices(),
                parts.hints(),
                parts.solution(directive, context),
                directive.attributes().positiveInteger("minutes"),
                directive.attributes().flag("hook"),
                context.position(directive.line())));
    }
}
