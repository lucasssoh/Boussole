package io.github.lucasssoh.boussole.format.parse.answer;

import io.github.lucasssoh.boussole.format.diagnostic.Code;
import io.github.lucasssoh.boussole.format.model.answer.Answer;
import io.github.lucasssoh.boussole.format.model.answer.NumericAnswer;
import io.github.lucasssoh.boussole.format.model.answer.Rational;
import io.github.lucasssoh.boussole.format.model.block.ExerciseType;
import io.github.lucasssoh.boussole.format.parse.Attributes;
import io.github.lucasssoh.boussole.format.parse.InvalidAttributeException;
import io.github.lucasssoh.boussole.format.parse.ParseContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** One value per line; {@code tolerance} and {@code unit} as attributes. */
final class NumericAnswerParser implements AnswerParser {

    @Override
    public ExerciseType type() {
        return ExerciseType.NUMERIC;
    }

    @Override
    public Set<String> attributes() {
        return Set.of("tolerance", "unit");
    }

    @Override
    public Optional<Answer> parse(List<AnswerLine> lines, Attributes attributes, ParseContext context) {
        Rational tolerance = attributes.text("tolerance")
                .map(value -> Rational.parse(value)
                        .filter(number -> number.compareTo(Rational.ZERO) >= 0)
                        .orElseThrow(() -> new InvalidAttributeException("tolerance", "must be a number, 0 or more, not `" + value + "`")))
                .orElse(Rational.ZERO);
        List<Rational> values = new ArrayList<>();
        for (AnswerLine line : lines) {
            Optional<Rational> value = Rational.parse(line.text());
            if (value.isEmpty()) {
                context.report(Code.F024, line.line(), "`" + line.text() + "` is not a number (write 2.5 or 3/4)");
                return Optional.empty();
            }
            values.add(value.get());
        }
        return Optional.of(new NumericAnswer(List.copyOf(values), tolerance, attributes.text("unit")));
    }
}
