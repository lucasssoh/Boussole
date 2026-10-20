package io.github.lucasssoh.boussole.format.parse.answer;

import io.github.lucasssoh.boussole.format.model.answer.Answer;
import io.github.lucasssoh.boussole.format.model.answer.SingleAnswer;
import io.github.lucasssoh.boussole.format.model.block.ExerciseType;
import io.github.lucasssoh.boussole.format.parse.Attributes;
import io.github.lucasssoh.boussole.format.parse.ParseContext;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** One accepted spelling per line. */
final class TextAnswerParser extends LineAnswerParser<String> {

    @Override
    public ExerciseType type() {
        return ExerciseType.TEXT;
    }

    @Override
    public Set<String> attributes() {
        return Set.of("strict");
    }

    @Override
    protected Optional<String> readLine(AnswerLine line, Attributes attributes, ParseContext context) {
        return Optional.of(line.text());
    }

    @Override
    protected Optional<Answer> assemble(List<String> spellings, Attributes attributes, AnswerLine first, ParseContext context) {
        return ExpectationReader.text(spellings, attributes.flag("strict"), first.line(), context).map(SingleAnswer::new);
    }
}
