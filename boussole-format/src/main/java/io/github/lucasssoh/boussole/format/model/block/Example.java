package io.github.lucasssoh.boussole.format.model.block;

import io.github.lucasssoh.boussole.format.diagnostic.Position;
import java.util.List;

/** A worked example, solution included and visible. */
public record Example(String label, List<Block> content, Position position) implements Block {
}
