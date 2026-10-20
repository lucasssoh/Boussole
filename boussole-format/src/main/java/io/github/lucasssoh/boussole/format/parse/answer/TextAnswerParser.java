package io.github.lucasssoh.boussole.format.parse.answer;

import io.github.lucasssoh.boussole.format.model.answer.AcceptedText;
import io.github.lucasssoh.boussole.format.model.answer.Answer;
import io.github.lucasssoh.boussole.format.model.answer.TextAnswer;
import io.github.lucasssoh.boussole.format.model.block.ExerciseType;
import io.github.lucasssoh.boussole.format.parse.Attributes;
import io.github.lucasssoh.boussole.format.parse.ParseContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** One accepted answer per line. */
final class TextAnswerParser implements AnswerParser {

    @Override
    public ExerciseType type() {
        return ExerciseType.TEXT;
    }

    @Override
    public Set<String> attributes() {
        return Set.of("strict");
    }

    @Override
    public Optional<Answer> parse(List<AnswerLine> lines, Attributes attributes, ParseContext context) {
        boolean strict = attributes.flag("strict");
        List<AcceptedText> accepted = new ArrayList<>();
        for (AnswerLine line : lines) {
            Optional<AcceptedText> text = AcceptedTexts.read(line.text(), strict, line.line(), context);
            if (text.isEmpty()) {
                return Optional.empty();
            }
            accepted.add(text.get());
        }
        return Optional.of(new TextAnswer(List.copyOf(accepted), strict));
    }
}
