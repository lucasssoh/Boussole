package io.github.lucasssoh.boussole.format.parse.answer;

import io.github.lucasssoh.boussole.format.model.answer.Answer;
import io.github.lucasssoh.boussole.format.model.answer.SingleAnswer;
import io.github.lucasssoh.boussole.format.model.block.ExerciseType;
import io.github.lucasssoh.boussole.format.model.expectation.NumberExpectation;
import io.github.lucasssoh.boussole.format.model.expectation.Rational;
import io.github.lucasssoh.boussole.format.parse.Attributes;
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
    protected Optional<Rational> readLine(AnswerLine line, Attributes attributes, ParseContext context) {
        return Numbers.read(line.text(), line.line(), context);
    }

    @Override
    protected Optional<Answer> assemble(List<Rational> values, Attributes attributes, AnswerLine first, ParseContext context) {
        return Optional.of(new SingleAnswer(
                new NumberExpectation(values, Numbers.toleranceAttribute(attributes), attributes.text("unit"))));
    }
}
