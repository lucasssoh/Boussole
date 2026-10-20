package io.github.lucasssoh.boussole.format.model;

import io.github.lucasssoh.boussole.format.diagnostic.SourcePosition;
import java.util.List;
import java.util.Optional;

/**
 * An entry of {@code cursus/sources.yaml}: a document of the sources folder (or a glob pattern
 * naming several, such as {@code Annales/**}), what the course did with it, and the SHA-256 of
 * the document as the course used it, which tells when it changes.
 */
public record SourceEntry(
        String file, Optional<String> sha256, SourceStatus status, List<String> chapters, String note, SourcePosition position) {

    public boolean isPattern() {
        return file.contains("*") || file.contains("?") || file.contains("{");
    }
}
