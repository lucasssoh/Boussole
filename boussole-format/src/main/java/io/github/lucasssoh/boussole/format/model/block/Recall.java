package io.github.lucasssoh.boussole.format.model.block;

import io.github.lucasssoh.boussole.format.diagnostic.Position;
import java.util.List;

/** A reminder of a term or prerequisite, just before it is needed. */
public record Recall(List<Block> content, Position position) implements Block {
}
