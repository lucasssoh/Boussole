package io.github.lucasssoh.boussole.format.model.block;

import io.github.lucasssoh.boussole.format.diagnostic.Position;
import java.util.List;

/** One tier of help, revealed in file order. */
public record Hint(List<Block> content, Position position) {
}
