package io.github.lucasssoh.boussole.format.model.part;

import io.github.lucasssoh.boussole.format.diagnostic.Position;
import io.github.lucasssoh.boussole.format.model.block.Block;
import java.util.List;

/** Five minutes on the previous session of the same subject, first. */
public record Warmup(List<Block> blocks, Position position) implements Part {
}
