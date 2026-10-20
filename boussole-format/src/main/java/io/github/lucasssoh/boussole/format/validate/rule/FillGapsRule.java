package io.github.lucasssoh.boussole.format.validate.rule;

import io.github.lucasssoh.boussole.format.diagnostic.Code;
import io.github.lucasssoh.boussole.format.diagnostic.Diagnostics;
import io.github.lucasssoh.boussole.format.model.answer.FillAnswer;
import io.github.lucasssoh.boussole.format.model.answer.Gap;
import io.github.lucasssoh.boussole.format.model.block.Exercise;
import io.github.lucasssoh.boussole.format.model.block.Prose;
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
    public void check(ValidationContext context, Diagnostics diagnostics) {
        context.course().blocks()
                .filter(Exercise.class::isInstance)
                .map(Exercise.class::cast)
                .forEach(exercise -> exercise.answer().ifPresent(answer -> {
                    if (answer instanceof FillAnswer fill) {
                        check(exercise, fill, diagnostics);
                    }
                }));
    }

    private static void check(Exercise exercise, FillAnswer answer, Diagnostics diagnostics) {
        Set<Integer> inStatement = new TreeSet<>();
        exercise.statement().stream()
                .filter(Prose.class::isInstance)
                .map(prose -> GAP.matcher(((Prose) prose).markdown()))
                .forEach(matcher -> addAll(matcher, inStatement));
        Set<Integer> inAnswer = answer.gaps().stream().map(Gap::number).collect(Collectors.toCollection(TreeSet::new));
        if (!inStatement.equals(inAnswer)) {
            diagnostics.report(Code.F023, exercise.position(), "exercise `" + exercise.id() + "`: the statement has gaps "
                    + inStatement + ", the answer " + inAnswer);
        }
    }

    private static void addAll(Matcher matcher, Set<Integer> gaps) {
        while (matcher.find()) {
            gaps.add(Integer.parseInt(matcher.group(1)));
        }
    }
}
