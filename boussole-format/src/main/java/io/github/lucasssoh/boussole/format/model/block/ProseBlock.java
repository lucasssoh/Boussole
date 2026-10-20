package io.github.lucasssoh.boussole.format.model.block;

import io.github.lucasssoh.boussole.format.diagnostic.SourcePosition;
import java.util.List;

/**
 * Plain Markdown, kept as written: the interface renders it. The inline directives it contains
 * are extracted so they can be checked.
 */
public record ProseBlock(String markdown, List<InlineDirective> inlines, SourcePosition position) implements Block {
}
