package io.github.lucasssoh.boussole.format.parse.answer;

import io.github.lucasssoh.boussole.format.diagnostic.Code;
import io.github.lucasssoh.boussole.format.model.answer.Answer;
import io.github.lucasssoh.boussole.format.model.answer.FillAnswer;
import io.github.lucasssoh.boussole.format.model.answer.Gap;
import io.github.lucasssoh.boussole.format.model.block.ExerciseType;
import io.github.lucasssoh.boussole.format.parse.Attributes;
import io.github.lucasssoh.boussole.format.parse.ParseContext;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** One line per gap, {@code N: …}, each read by {@link ExpectationReader}. */
final class FillAnswerParser extends LineAnswerParser<Gap> {

    private static final Pattern GAP_LINE = Pattern.compile("(\\d+)\\s*:\\s*(.+)");

    @Override
    public ExerciseType type() {
        return ExerciseType.FILL;
    }

    @Override
    public Set<String> attributes() {
        return Set.of("strict");
    }

    @Override
    protected Optional<Gap> readLine(AnswerLine line, Attributes attributes, ParseContext context) {
        Matcher matcher = GAP_LINE.matcher(line.text());
        if (!matcher.matches()) {
            context.report(Code.F021, line.line(), "a fill answer line reads `1: answer | other answer`, `2: number 62` or `3: choice *right | wrong`");
            return Optional.empty();
        }
        int number = Integer.parseInt(matcher.group(1));
        return ExpectationReader.read(matcher.group(2), attributes.flag("strict"), line.line(), context)
                .map(expected -> new Gap(number, expected));
    }

    @Override
    protected Optional<Answer> assemble(List<Gap> gaps, Attributes attributes, AnswerLine first, ParseContext context) {
        Set<Integer> seen = new HashSet<>();
        for (Gap gap : gaps) {
            if (!seen.add(gap.number())) {
                context.report(Code.F021, first.line(), "gap " + gap.number() + " is answered twice");
                return Optional.empty();
            }
        }
        return Optional.of(new FillAnswer(gaps));
    }
}
