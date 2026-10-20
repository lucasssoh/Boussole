package io.github.lucasssoh.boussole.format.model;

import io.github.lucasssoh.boussole.format.diagnostic.SourcePosition;
import java.util.List;

public record SyllabusChapter(
        String id, String title, String dir, List<SourceReference> sources, List<SyllabusSession> sessions, SourcePosition position) {
}
