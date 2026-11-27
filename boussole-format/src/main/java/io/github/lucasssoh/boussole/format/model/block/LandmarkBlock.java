package io.github.lucasssoh.boussole.format.model.block;

import io.github.lucasssoh.boussole.format.diagnostic.SourcePosition;
import java.util.List;

/**
 * What a whole track keeps coming back to (the program of the running example, a figure, a
 * table, an algorithm): shown where it is written, then kept in view beside the steps of every
 * session that names it in its {@code landmarks}.
 */
public record LandmarkBlock(String id, String title, List<Block> content, SourcePosition position) implements Block {
}
