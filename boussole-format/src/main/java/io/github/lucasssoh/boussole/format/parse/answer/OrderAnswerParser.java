package io.github.lucasssoh.boussole.format.parse.answer;

import io.github.lucasssoh.boussole.format.diagnostic.Code;
import io.github.lucasssoh.boussole.format.model.answer.Answer;
import io.github.lucasssoh.boussole.format.model.answer.OrderAnswer;
import io.github.lucasssoh.boussole.format.model.block.ExerciseType;
import io.github.lucasssoh.boussole.format.parse.Attributes;
import io.github.lucasssoh.boussole.format.parse.ParseContext;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** A Markdown list, in the right order. */
final class OrderAnswerParser extends LineAnswerParser<String> {

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
    protected int minimumItems() {
        return 2;
    }

    @Override
    protected Optional<String> readLine(AnswerLine line, Attributes attributes, ParseContext context) {
        Matcher matcher = ITEM.matcher(line.text());
        if (!matcher.matches()) {
            context.report(Code.F021, line.line(), "an order answer is a list: `1. first item`");
            return Optional.empty();
        }
        return Optional.of(matcher.group(1).strip());
    }

    @Override
    protected Optional<Answer> assemble(List<String> items, Attributes attributes, AnswerLine first, ParseContext context) {
        return Optional.of(new OrderAnswer(items));
    }
}
