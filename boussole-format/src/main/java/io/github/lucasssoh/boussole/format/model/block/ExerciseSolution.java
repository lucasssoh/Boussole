package io.github.lucasssoh.boussole.format.model.block;

import io.github.lucasssoh.boussole.format.diagnostic.SourcePosition;
import java.util.List;

/** The worked solution, shown after the first attempt. */
public record ExerciseSolution(List<Block> content, SourcePosition position) {
}
