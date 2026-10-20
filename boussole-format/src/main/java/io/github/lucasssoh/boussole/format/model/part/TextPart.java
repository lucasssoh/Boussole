package io.github.lucasssoh.boussole.format.model.part;

import io.github.lucasssoh.boussole.format.diagnostic.SourcePosition;
import io.github.lucasssoh.boussole.format.model.block.Block;
import java.util.List;

/** Content outside any notion or section, such as an introduction. */
public record TextPart(List<Block> blocks, SourcePosition position) implements Part {
}
