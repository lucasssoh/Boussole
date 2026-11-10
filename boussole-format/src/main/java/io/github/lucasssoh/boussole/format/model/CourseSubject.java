package io.github.lucasssoh.boussole.format.model;

import io.github.lucasssoh.boussole.format.diagnostic.SourcePosition;
import java.util.List;
import java.util.Optional;

/**
 * What {@code subject.yaml} says about a subject, with the authors and contributors it credits,
 * how demanding it is and its field of knowledge. {@code codeColors} is false when the code of the
 * track is shown in black and white, as its exam prints it ({@code code_colors: false}).
 */
public record CourseSubject(
        String id,
        String name,
        String lang,
        String sourcesDir,
        Optional<String> description,
        List<SubjectCredit> authors,
        List<SubjectCredit> contributors,
        Optional<TrackLevel> level,
        Optional<Domain> domain,
        boolean codeColors,
        SourcePosition position) {
}
