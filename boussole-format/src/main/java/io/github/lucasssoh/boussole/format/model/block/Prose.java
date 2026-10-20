package io.github.lucasssoh.boussole.format.model.block;

import io.github.lucasssoh.boussole.format.diagnostic.Position;
import java.util.List;

/**
 * Plain Markdown, kept as written: the interface renders it. The inline directives it contains
 * are extracted so they can be checked.
 */
public record Prose(String markdown, List<InlineDirective> inlines, Position position) implements Block {
}
