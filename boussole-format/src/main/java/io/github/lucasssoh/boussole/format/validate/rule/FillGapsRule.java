package io.github.lucasssoh.boussole.format.validate.rule;

import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCode;
import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCollector;
import io.github.lucasssoh.boussole.format.model.answer.FillAnswer;
import io.github.lucasssoh.boussole.format.model.answer.FillGap;
import io.github.lucasssoh.boussole.format.model.block.ExerciseBlock;
import io.github.lucasssoh.boussole.format.model.block.ProseBlock;
import io.github.lucasssoh.boussole.format.validate.ValidationContext;
import io.github.lucasssoh.boussole.format.validate.ValidationRule;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/** F023: the gaps {@code [[n]]} of a fill statement are exactly the gaps of its answer. */
public final class FillGapsRule implements ValidationRule {

    private static final Pattern GAP = Pattern.compile("\\[\\[(\\d+)]]");

    @Override
    public void check(ValidationContext context, DiagnosticCollector diagnostics) {
        context.course().blocks()
                .filter(ExerciseBlock.class::isInstance)
                .map(ExerciseBlock.class::cast)
                .forEach(exercise -> exercise.answer().ifPresent(answer -> {
                    if (answer instanceof FillAnswer fill) {
                        check(exercise, fill, diagnostics);
                    }
                }));
    }

    private static void check(ExerciseBlock exercise, FillAnswer answer, DiagnosticCollector diagnostics) {
        Set<Integer> inStatement = new TreeSet<>();
        exercise.statement().stream()
                .filter(ProseBlock.class::isInstance)
                .map(prose -> GAP.matcher(((ProseBlock) prose).markdown()))
                .forEach(matcher -> addAll(matcher, inStatement));
        Set<Integer> inAnswer = answer.gaps().stream().map(FillGap::number).collect(Collectors.toCollection(TreeSet::new));
        if (!inStatement.equals(inAnswer)) {
            diagnostics.report(DiagnosticCode.F023, exercise.position(), "exercise `" + exercise.id() + "`: the statement has gaps "
                    + inStatement + ", the answer " + inAnswer);
        }
    }

    private static void addAll(Matcher matcher, Set<Integer> gaps) {
        while (matcher.find()) {
            gaps.add(Integer.parseInt(matcher.group(1)));
        }
    }
}
