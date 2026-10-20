package io.github.lucasssoh.boussole.format.model;

import io.github.lucasssoh.boussole.format.model.block.Block;
import io.github.lucasssoh.boussole.format.model.block.Blocks;
import io.github.lucasssoh.boussole.format.model.part.Part;
import java.util.List;
import java.util.stream.Stream;

/** The content of one session file. */
public record Session(SessionHeader header, List<Part> parts) {

    /** Every block of the session, nested ones included, in reading order. */
    public Stream<Block> blocks() {
        return parts.stream().flatMap(part -> Blocks.walk(part.blocks()));
    }
}
