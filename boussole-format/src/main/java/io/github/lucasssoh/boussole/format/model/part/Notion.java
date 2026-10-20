package io.github.lucasssoh.boussole.format.model.part;

import io.github.lucasssoh.boussole.format.diagnostic.Position;
import io.github.lucasssoh.boussole.format.model.block.Block;
import java.util.List;

/** A level-2 heading with an ID and everything up to the next one. */
public record Notion(String id, String title, List<Block> blocks, Position position) implements Part {
}
