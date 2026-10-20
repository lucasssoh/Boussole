package io.github.lucasssoh.boussole.format.model.block;

import io.github.lucasssoh.boussole.format.diagnostic.Position;
import java.util.List;

/** The worked solution, shown after the first attempt. */
public record Solution(List<Block> content, Position position) {
}
