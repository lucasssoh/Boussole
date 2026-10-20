package io.github.lucasssoh.boussole.format.model.block;

import io.github.lucasssoh.boussole.format.diagnostic.Position;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

/** A multiple-choice exercise: the ticked choices are the answer. */
public record Qcm(
        String id,
        boolean multiple,
        List<Block> statement,
        List<Choice> choices,
        List<Hint> hints,
        Optional<Solution> solution,
        OptionalInt minutes,
        Position position)
        implements Block {
}
