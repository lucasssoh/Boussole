package io.github.lucasssoh.boussole.format.model;

import io.github.lucasssoh.boussole.format.diagnostic.SourcePosition;
import java.util.OptionalInt;

/** A file of the subject's sources, optionally at a page. */
public record SourceReference(String file, OptionalInt page, SourcePosition position) {
}
