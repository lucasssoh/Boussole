package io.github.lucasssoh.boussole.format.model.block;

import io.github.lucasssoh.boussole.format.diagnostic.SourcePosition;
import java.util.List;

/** A reminder of a term or prerequisite, just before it is needed. */
public record RecallBlock(List<Block> content, SourcePosition position) implements Block {
}
