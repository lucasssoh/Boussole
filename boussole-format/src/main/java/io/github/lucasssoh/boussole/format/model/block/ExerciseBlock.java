package io.github.lucasssoh.boussole.format.model.block;

import io.github.lucasssoh.boussole.format.diagnostic.SourcePosition;
import io.github.lucasssoh.boussole.format.model.answer.Answer;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * An exercise checked by Boussole. Its answer is empty for a {@link ExerciseType#FREE} exercise,
 * or when it could not be read. {@code minutes} overrides the time estimated by {@link QuestionTimeEstimator}.
 */
public record ExerciseBlock(
        String id,
        ExerciseType type,
        List<Block> statement,
        Optional<Answer> answer,
        List<ExerciseHint> hints,
        Optional<ExerciseSolution> solution,
        OptionalInt minutes,
        SourcePosition position)
        implements Block {
}
