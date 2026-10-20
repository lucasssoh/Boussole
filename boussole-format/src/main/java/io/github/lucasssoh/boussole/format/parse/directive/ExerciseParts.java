package io.github.lucasssoh.boussole.format.parse.directive;

import io.github.lucasssoh.boussole.format.diagnostic.Code;
import io.github.lucasssoh.boussole.format.model.block.Hint;
import io.github.lucasssoh.boussole.format.model.block.Solution;
import io.github.lucasssoh.boussole.format.parse.ParseContext;
import io.github.lucasssoh.boussole.format.parse.RawNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * The content of an {@code exercise} or a {@code qcm}, split into its statement and its
 * {@code answer}, {@code hint} and {@code solution} parts.
 */
record ExerciseParts(
        List<RawNode> statement, List<RawNode.Container> answers, List<Hint> hints, List<Solution> solutions) {

    static ExerciseParts split(Directive directive, ParseContext context) {
        List<RawNode> statement = new ArrayList<>();
        List<RawNode.Container> answers = new ArrayList<>();
        List<Hint> hints = new ArrayList<>();
        List<Solution> solutions = new ArrayList<>();
        for (RawNode child : directive.children()) {
            if (child instanceof RawNode.Container part && Parents.EXERCISE_PARTS.contains(part.directive().name())) {
                String name = part.directive().name();
                context.attributes(name, part.directive().attributes(), Set.of(), Set.of(), part.line());
                var position = context.position(part.line());
                switch (name) {
                    case "answer" -> answers.add(part);
                    case "hint" -> hints.add(new Hint(context.blocks(part.children(), name), position));
                    default -> solutions.add(new Solution(context.blocks(part.children(), name), position));
                }
            } else {
                statement.add(child);
            }
        }
        return new ExerciseParts(List.copyOf(statement), List.copyOf(answers), List.copyOf(hints), List.copyOf(solutions));
    }

    /** The single solution, after reporting when there is none or several. */
    Optional<Solution> solution(Directive directive, ParseContext context) {
        if (solutions.size() != 1) {
            context.report(Code.F021, directive.line(), "`" + directive.name() + "` needs exactly one `solution`, it has "
                    + solutions.size());
        }
        return solutions.stream().findFirst();
    }
}
