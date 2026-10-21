package io.github.lucasssoh.boussole.format.model;

import io.github.lucasssoh.boussole.format.diagnostic.SourcePosition;
import java.util.List;
import java.util.Optional;

/** What {@code subject.yaml} says about a subject, with the authors and contributors it credits. */
public record CourseSubject(
        String id,
        String name,
        String lang,
        String sourcesDir,
        Optional<String> description,
        List<SubjectCredit> authors,
        List<SubjectCredit> contributors,
        SourcePosition position) {
}
