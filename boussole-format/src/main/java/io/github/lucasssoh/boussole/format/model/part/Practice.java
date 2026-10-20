package io.github.lucasssoh.boussole.format.model.part;

import io.github.lucasssoh.boussole.format.diagnostic.Position;
import io.github.lucasssoh.boussole.format.model.block.Block;
import java.util.List;

/** The final, longer exercises, last. */
public record Practice(List<Block> blocks, Position position) implements Part {
}
