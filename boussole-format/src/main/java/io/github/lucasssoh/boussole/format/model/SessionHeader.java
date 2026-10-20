package io.github.lucasssoh.boussole.format.model;

import io.github.lucasssoh.boussole.format.diagnostic.Position;
import java.util.List;

/** The front matter of a session file. */
public record SessionHeader(
        String id,
        List<String> objectives,
        List<String> prerequisites,
        List<String> terms,
        List<SourceRef> sources,
        Position position) {
}
