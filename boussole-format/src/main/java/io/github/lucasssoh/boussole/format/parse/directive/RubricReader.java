package io.github.lucasssoh.boussole.format.parse.directive;

import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCode;
import io.github.lucasssoh.boussole.format.model.block.ExerciseRubric;
import io.github.lucasssoh.boussole.format.model.block.ExerciseType;
import io.github.lucasssoh.boussole.format.model.block.RubricCriterion;
import io.github.lucasssoh.boussole.format.parse.ParseContext;
import io.github.lucasssoh.boussole.format.parse.RawNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The {@code :::rubric} of a free exercise: one criterion per list item, its weight in brackets,
 * {@code - [2] La classe abstraite déclare créeMotif()}. A line indented under an item goes on with it.
 */
final class RubricReader {

    private static final Pattern ITEM = Pattern.compile("^[-*]\\s+(.*)$");
    private static final Pattern WEIGHTED = Pattern.compile("^\\[\\s*([^\\]]*?)\\s*\\]\\s+(\\S.*)$");
    private static final Pattern NUMBER = Pattern.compile("\\d+(?:[.,]\\d+)?");
    private static final String SHAPE = "write each criterion as `- [2] what the corrector checks`, its weight in brackets";

    private RubricReader() {
    }

    static Optional<ExerciseRubric> read(ExerciseType type, List<RawNode.Container> rubrics, ParseContext context) {
        if (rubrics.isEmpty()) {
            return Optional.empty();
        }
        RawNode.Container rubric = rubrics.getFirst();
        if (type != ExerciseType.FREE) {
            context.report(DiagnosticCode.F047, rubric.line(), "a " + type.attributeValue()
                    + " exercise is checked by Boussole: remove the `rubric`, or make the exercise `type=free`");
            return Optional.empty();
        }
        if (rubrics.size() > 1) {
            context.report(DiagnosticCode.F048, rubrics.get(1).line(), "a free exercise has one `rubric`: put every criterion in the first");
        }
        List<RubricCriterion> criteria = new ArrayList<>();
        boolean readable = true;
        StringBuilder text = null;
        double weight = 0;
        for (RawNode child : rubric.children()) {
            if (!(child instanceof RawNode.Text lines)) {
                context.report(DiagnosticCode.F048, child.line(), "a `rubric` holds only its list of criteria: " + SHAPE);
                readable = false;
                continue;
            }
            for (int index = 0; index < lines.lines().size(); index++) {
                String line = lines.lines().get(index);
                int number = lines.line() + index;
                if (line.isBlank()) {
                    continue;
                }
                Matcher item = ITEM.matcher(line.strip());
                if (!Character.isWhitespace(line.charAt(0)) && item.matches()) {
                    if (text != null) {
                        criteria.add(new RubricCriterion(text.toString(), weight));
                    }
                    text = null;
                    Matcher weighted = WEIGHTED.matcher(item.group(1).strip());
                    if (!weighted.matches()) {
                        context.report(DiagnosticCode.F048, number, "criterion without a weight: " + SHAPE);
                        readable = false;
                        continue;
                    }
                    String value = weighted.group(1);
                    double parsed = NUMBER.matcher(value).matches() ? Double.parseDouble(value.replace(',', '.')) : 0;
                    if (parsed <= 0) {
                        context.report(DiagnosticCode.F048, number, "the weight `" + value
                                + "` is not a number above 0: write `[1]`, `[2]` or `[0.5]`, the points the corrector gives");
                        readable = false;
                        continue;
                    }
                    weight = parsed;
                    text = new StringBuilder(weighted.group(2).strip());
                } else if (text != null && Character.isWhitespace(line.charAt(0))) {
                    text.append(' ').append(line.strip());
                } else {
                    context.report(DiagnosticCode.F048, number, "`" + line.strip() + "` is not a criterion: " + SHAPE);
                    readable = false;
                }
            }
        }
        if (text != null) {
            criteria.add(new RubricCriterion(text.toString(), weight));
        }
        if (criteria.isEmpty()) {
            if (readable) {
                context.report(DiagnosticCode.F048, rubric.line(), "the `rubric` is empty: " + SHAPE);
            }
            return Optional.empty();
        }
        return readable ? Optional.of(new ExerciseRubric(criteria, context.position(rubric.line()))) : Optional.empty();
    }
}
