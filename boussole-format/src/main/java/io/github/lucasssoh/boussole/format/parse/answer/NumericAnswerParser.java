package io.github.lucasssoh.boussole.format.parse.answer;

import io.github.lucasssoh.boussole.format.model.answer.Answer;
import io.github.lucasssoh.boussole.format.model.answer.SingleAnswer;
import io.github.lucasssoh.boussole.format.model.block.ExerciseType;
import io.github.lucasssoh.boussole.format.model.expectation.NumberExpectation;
import io.github.lucasssoh.boussole.format.model.expectation.Rational;
import io.github.lucasssoh.boussole.format.parse.DirectiveAttributes;
import io.github.lucasssoh.boussole.format.parse.ParseContext;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** One accepted value per line; {@code tolerance} and {@code unit} as attributes. */
final class NumericAnswerParser extends LineAnswerParser<Rational> {

    @Override
    public ExerciseType type() {
        return ExerciseType.NUMERIC;
    }

    @Override
    public Set<String> attributes() {
        return Set.of("tolerance", "unit");
    }

    @Override
    protected Optional<Rational> readLine(AnswerLine line, DirectiveAttributes attributes, ParseContext context) {
        return NumberReader.read(line.text(), line.line(), context);
    }

    @Override
    protected Optional<Answer> assemble(List<Rational> values, DirectiveAttributes attributes, AnswerLine first, ParseContext context) {
        return Optional.of(new SingleAnswer(
                new NumberExpectation(values, NumberReader.toleranceAttribute(attributes), attributes.text("unit"))));
    }
}
