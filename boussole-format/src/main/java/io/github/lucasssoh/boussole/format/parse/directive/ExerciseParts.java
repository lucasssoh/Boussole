package io.github.lucasssoh.boussole.format.parse.directive;

import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCode;
import io.github.lucasssoh.boussole.format.model.block.ExerciseHint;
import io.github.lucasssoh.boussole.format.model.block.ExerciseSolution;
import io.github.lucasssoh.boussole.format.parse.ParseContext;
import io.github.lucasssoh.boussole.format.parse.RawNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * The content of an {@code exercise} or a {@code mcq}, split into its statement and its
 * {@code answer}, {@code hint} and {@code solution} parts.
 */
record ExerciseParts(
        List<RawNode> statement, List<RawNode.Container> answers, List<ExerciseHint> hints, List<ExerciseSolution> solutions) {

    static ExerciseParts split(CheckedDirective directive, ParseContext context) {
        List<RawNode> statement = new ArrayList<>();
        List<RawNode.Container> answers = new ArrayList<>();
        List<ExerciseHint> hints = new ArrayList<>();
        List<ExerciseSolution> solutions = new ArrayList<>();
        for (RawNode child : directive.children()) {
            if (child instanceof RawNode.Container part && DirectivePlaces.EXERCISE_PARTS.contains(part.directive().name())) {
                String name = part.directive().name();
                context.attributes(name, part.directive().attributes(), Set.of(), Set.of(), part.line());
                var position = context.position(part.line());
                switch (name) {
                    case "answer" -> answers.add(part);
                    case "hint" -> hints.add(new ExerciseHint(context.blocks(part.children(), name), position));
                    default -> solutions.add(new ExerciseSolution(context.blocks(part.children(), name), position));
                }
            } else {
                statement.add(child);
            }
        }
        return new ExerciseParts(List.copyOf(statement), List.copyOf(answers), List.copyOf(hints), List.copyOf(solutions));
    }

    /** The single solution, after reporting when there is none or several. */
    Optional<ExerciseSolution> solution(CheckedDirective directive, ParseContext context) {
        if (solutions.size() != 1) {
            context.report(DiagnosticCode.F021, directive.line(), "`" + directive.name() + "` needs exactly one `solution`, it has "
                    + solutions.size());
        }
        return solutions.stream().findFirst();
    }
}
