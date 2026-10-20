package io.github.lucasssoh.boussole.format.parse.answer;

import io.github.lucasssoh.boussole.format.diagnostic.Code;
import io.github.lucasssoh.boussole.format.model.answer.AcceptedText;
import io.github.lucasssoh.boussole.format.model.answer.Answer;
import io.github.lucasssoh.boussole.format.model.answer.FillAnswer;
import io.github.lucasssoh.boussole.format.model.answer.Gap;
import io.github.lucasssoh.boussole.format.model.block.ExerciseType;
import io.github.lucasssoh.boussole.format.parse.Attributes;
import io.github.lucasssoh.boussole.format.parse.ParseContext;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** One line per gap: {@code 1: answer | other accepted answer}. */
final class FillAnswerParser implements AnswerParser {

    private static final Pattern GAP_LINE = Pattern.compile("(\\d+)\\s*:\\s*(.+)");
    private static final Pattern ALTERNATIVES = Pattern.compile("\\s+\\|\\s+");

    @Override
    public ExerciseType type() {
        return ExerciseType.FILL;
    }

    @Override
    public Set<String> attributes() {
        return Set.of("strict");
    }

    @Override
    public Optional<Answer> parse(List<AnswerLine> lines, Attributes attributes, ParseContext context) {
        boolean strict = attributes.flag("strict");
        List<Gap> gaps = new ArrayList<>();
        Set<Integer> seen = new HashSet<>();
        for (AnswerLine line : lines) {
            Matcher matcher = GAP_LINE.matcher(line.text());
            if (!matcher.matches()) {
                context.report(Code.F021, line.line(), "a fill answer line reads `1: answer | other answer`");
                return Optional.empty();
            }
            int number = Integer.parseInt(matcher.group(1));
            if (!seen.add(number)) {
                context.report(Code.F021, line.line(), "gap " + number + " is answered twice");
                return Optional.empty();
            }
            List<AcceptedText> accepted = new ArrayList<>();
            for (String alternative : ALTERNATIVES.split(matcher.group(2).strip())) {
                Optional<AcceptedText> text = AcceptedTexts.read(alternative, strict, line.line(), context);
                if (text.isEmpty()) {
                    return Optional.empty();
                }
                accepted.add(text.get());
            }
            gaps.add(new Gap(number, List.copyOf(accepted)));
        }
        return Optional.of(new FillAnswer(List.copyOf(gaps), strict));
    }
}
