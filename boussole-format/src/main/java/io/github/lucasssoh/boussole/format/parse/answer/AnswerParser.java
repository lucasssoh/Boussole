package io.github.lucasssoh.boussole.format.parse.answer;

import io.github.lucasssoh.boussole.format.model.answer.Answer;
import io.github.lucasssoh.boussole.format.model.block.ExerciseType;
import io.github.lucasssoh.boussole.format.parse.DirectiveAttributes;
import io.github.lucasssoh.boussole.format.parse.ParseContext;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** Reads the {@code answer} part of one type of exercise. */
public interface AnswerParser {

    ExerciseType type();

    /** The attributes of the exercise that only this type accepts. */
    Set<String> attributes();

    /** @param lines at least one line */
    Optional<Answer> parse(List<AnswerLine> lines, DirectiveAttributes attributes, ParseContext context);
}
