package io.github.lucasssoh.boussole.format.model.block;

import io.github.lucasssoh.boussole.format.diagnostic.Position;
import java.util.List;

public record Callout(CalloutKind kind, List<Block> content, Position position) implements Block {
}
