package io.github.lucasssoh.boussole.format.model.part;

import io.github.lucasssoh.boussole.format.diagnostic.Position;
import io.github.lucasssoh.boussole.format.model.block.Block;
import java.util.List;

/** A short quiz on the whole session; passing it validates the session. */
public record Checkpoint(List<Block> blocks, Position position) implements Part {
}
