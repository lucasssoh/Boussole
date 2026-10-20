package io.github.lucasssoh.boussole.format.model.block;

import io.github.lucasssoh.boussole.format.diagnostic.SourcePosition;
import java.util.List;

public record CalloutBlock(CalloutKind kind, List<Block> content, SourcePosition position) implements Block {
}
