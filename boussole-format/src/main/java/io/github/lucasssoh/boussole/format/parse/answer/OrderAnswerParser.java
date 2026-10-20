package io.github.lucasssoh.boussole.format.parse.answer;

import io.github.lucasssoh.boussole.format.diagnostic.Code;
import io.github.lucasssoh.boussole.format.model.answer.Answer;
import io.github.lucasssoh.boussole.format.model.answer.OrderAnswer;
import io.github.lucasssoh.boussole.format.model.block.ExerciseType;
import io.github.lucasssoh.boussole.format.parse.Attributes;
import io.github.lucasssoh.boussole.format.parse.ParseContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** A Markdown list, in the right order. */
final class OrderAnswerParser implements AnswerParser {

    private static final Pattern ITEM = Pattern.compile("(?:\\d+[.)]|[-*+])\\s+(.+)");

    @Override
    public ExerciseType type() {
        return ExerciseType.ORDER;
    }

    @Override
    public Set<String> attributes() {
        return Set.of();
    }

    @Override
    public Optional<Answer> parse(List<AnswerLine> lines, Attributes attributes, ParseContext context) {
        List<String> items = new ArrayList<>();
        for (AnswerLine line : lines) {
            Matcher matcher = ITEM.matcher(line.text());
            if (!matcher.matches()) {
                context.report(Code.F021, line.line(), "an order answer is a list: `1. first item`");
                return Optional.empty();
            }
            items.add(matcher.group(1).strip());
        }
        if (items.size() < 2) {
            context.report(Code.F021, lines.getFirst().line(), "an order answer needs at least two items");
            return Optional.empty();
        }
        return Optional.of(new OrderAnswer(List.copyOf(items)));
    }
}
