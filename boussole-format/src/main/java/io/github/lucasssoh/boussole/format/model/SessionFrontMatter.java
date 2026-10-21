package io.github.lucasssoh.boussole.format.model;

import io.github.lucasssoh.boussole.format.diagnostic.SourcePosition;
import java.util.List;

/** The front matter of a session file; {@code writtenBy} says who wrote, revised or translated it. */
public record SessionFrontMatter(
        String id,
        List<String> objectives,
        List<String> prerequisites,
        List<String> terms,
        List<SourceReference> sources,
        List<Authorship> writtenBy,
        SourcePosition position) {
}
