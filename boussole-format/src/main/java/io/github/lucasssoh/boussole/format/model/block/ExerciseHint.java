package io.github.lucasssoh.boussole.format.model.block;

import io.github.lucasssoh.boussole.format.diagnostic.SourcePosition;
import java.util.List;

/** One tier of help, revealed in file order. */
public record ExerciseHint(List<Block> content, SourcePosition position) {
}
