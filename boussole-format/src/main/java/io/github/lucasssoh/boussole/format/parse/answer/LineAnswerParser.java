package io.github.lucasssoh.boussole.format.parse.answer;

import io.github.lucasssoh.boussole.format.diagnostic.Code;
import io.github.lucasssoh.boussole.format.model.answer.Answer;
import io.github.lucasssoh.boussole.format.parse.Attributes;
import io.github.lucasssoh.boussole.format.parse.ParseContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The reading every answer type shares: each line becomes one item, the first unreadable line
 * stops the reading (it has been reported), then the items make the answer. Subclasses say how
 * a line reads and what the items make.
 *
 * @param <T> what one line holds
 */
abstract class LineAnswerParser<T> implements AnswerParser {

    @Override
    public final Optional<Answer> parse(List<AnswerLine> lines, Attributes attributes, ParseContext context) {
        List<T> items = new ArrayList<>();
        for (AnswerLine line : lines) {
            Optional<T> item = readLine(line, attributes, context);
            if (item.isEmpty()) {
                return Optional.empty();
            }
            items.add(item.get());
        }
        if (items.size() < minimumItems()) {
            context.report(Code.F021, lines.getFirst().line(), "a " + type().attributeValue() + " answer needs at least "
                    + minimumItems() + " lines");
            return Optional.empty();
        }
        return assemble(List.copyOf(items), attributes, lines.getFirst(), context);
    }

    /** One line, or empty after reporting why it cannot be read. */
    protected abstract Optional<T> readLine(AnswerLine line, Attributes attributes, ParseContext context);

    /** The answer the items make, or empty after reporting why they cannot make one. */
    protected abstract Optional<Answer> assemble(List<T> items, Attributes attributes, AnswerLine first, ParseContext context);

    protected int minimumItems() {
        return 1;
    }
}
