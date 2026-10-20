package io.github.lucasssoh.boussole.format.model;

import io.github.lucasssoh.boussole.format.diagnostic.Position;
import java.util.OptionalInt;

/** A file of the subject's sources, optionally at a page. */
public record SourceRef(String file, OptionalInt page, Position position) {
}
