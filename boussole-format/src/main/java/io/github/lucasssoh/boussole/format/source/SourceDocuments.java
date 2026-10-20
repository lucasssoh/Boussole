package io.github.lucasssoh.boussole.format.source;

import java.util.OptionalInt;

/** The learner's documents of a subject, as far as the validator needs them. */
public interface SourceDocuments {

    boolean exists(String file);

    /** The number of pages of a PDF; empty for another kind of file, or one that cannot be read. */
    OptionalInt pageCount(String file);
}
