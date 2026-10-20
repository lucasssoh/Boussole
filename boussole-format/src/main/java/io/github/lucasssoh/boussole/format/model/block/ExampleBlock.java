package io.github.lucasssoh.boussole.format.model.block;

import io.github.lucasssoh.boussole.format.diagnostic.SourcePosition;
import java.util.List;

/** A worked example, solution included and visible. */
public record ExampleBlock(String label, List<Block> content, SourcePosition position) implements Block {
}
