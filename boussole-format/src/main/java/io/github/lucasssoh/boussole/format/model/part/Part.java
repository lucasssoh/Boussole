package io.github.lucasssoh.boussole.format.model.part;

import io.github.lucasssoh.boussole.format.diagnostic.Position;
import io.github.lucasssoh.boussole.format.model.block.Block;
import java.util.List;

/** A top-level part of a session, in the order of {@code FORMAT.md} §5.3. */
public sealed interface Part permits TextPart, Warmup, Notion, Checkpoint, Practice {

    List<Block> blocks();

    Position position();
}
