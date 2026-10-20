package io.github.lucasssoh.boussole.format.model;

import io.github.lucasssoh.boussole.format.diagnostic.Position;
import java.util.List;

public record Chapter(
        String id, String title, String dir, List<SourceRef> sources, List<SessionEntry> sessions, Position position) {
}
