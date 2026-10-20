package io.github.lucasssoh.boussole.format.model.part;

import io.github.lucasssoh.boussole.format.diagnostic.SourcePosition;
import io.github.lucasssoh.boussole.format.model.block.Block;
import java.util.List;

/** The final, longer exercises, last. */
public record PracticePart(List<Block> blocks, SourcePosition position) implements Part {
}
