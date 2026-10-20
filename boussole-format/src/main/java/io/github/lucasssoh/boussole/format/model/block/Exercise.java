package io.github.lucasssoh.boussole.format.model.block;

import io.github.lucasssoh.boussole.format.diagnostic.Position;
import io.github.lucasssoh.boussole.format.model.answer.Answer;
import java.util.List;
import java.util.Optional;

/**
 * An exercise checked by Boussole. Its answer is empty for a {@link ExerciseType#FREE} exercise,
 * or when it could not be read.
 */
public record Exercise(
        String id,
        ExerciseType type,
        List<Block> statement,
        Optional<Answer> answer,
        List<Hint> hints,
        Optional<Solution> solution,
        Position position)
        implements Block {
}
