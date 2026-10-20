package io.github.lucasssoh.boussole.format.source;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

/** The learner's documents of a subject, as far as the validator needs them. */
public interface SourceDocuments {

    boolean exists(String file);

    /** The number of pages of a PDF; empty for another kind of file, or one that cannot be read. */
    OptionalInt pageCount(String file);

    /** Every document of the sources folder, by its path relative to it with {@code /}, sorted. */
    List<String> documents();

    /** The SHA-256 of a document, in lowercase hexadecimal; empty when it cannot be read. */
    Optional<String> sha256(String file);
}
