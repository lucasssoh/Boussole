package io.github.lucasssoh.boussole.format.model.block;

import io.github.lucasssoh.boussole.format.diagnostic.SourcePosition;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * A multiple-choice exercise: the ticked choices are the answer. A {@code hook} is a guess on what
 * the session is about to teach, asked in its warmup, which earns no points.
 */
public record McqBlock(
        String id,
        boolean multiple,
        List<Block> statement,
        List<McqChoice> choices,
        List<ExerciseHint> hints,
        Optional<ExerciseSolution> solution,
        OptionalInt minutes,
        boolean hook,
        SourcePosition position)
        implements Block {
}
