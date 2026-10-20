package io.github.lucasssoh.boussole.format.validate;

import io.github.lucasssoh.boussole.format.model.Course;
import io.github.lucasssoh.boussole.format.source.SourceDocuments;
import java.util.Optional;

/** What the rules check: the course, its source documents, and a video catalog when videos are checked. */
public record ValidationContext(Course course, SourceDocuments sources, Optional<VideoCatalog> videos) {
}
