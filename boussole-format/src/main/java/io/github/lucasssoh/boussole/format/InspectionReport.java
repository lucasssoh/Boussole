package io.github.lucasssoh.boussole.format;

import io.github.lucasssoh.boussole.format.diagnostic.Code;
import io.github.lucasssoh.boussole.format.diagnostic.Diagnostic;
import io.github.lucasssoh.boussole.format.model.Course;
import java.util.List;
import java.util.Optional;

/** The course read from a subject folder, if any, and every problem found, sorted by file and line. */
public record InspectionReport(Optional<Course> course, List<Diagnostic> diagnostics) {

    public boolean hasErrors() {
        return diagnostics.stream().anyMatch(Diagnostic::isError);
    }

    public List<Diagnostic> errors() {
        return diagnostics.stream().filter(Diagnostic::isError).toList();
    }

    public List<Diagnostic> warnings() {
        return diagnostics.stream().filter(diagnostic -> !diagnostic.isError()).toList();
    }

    public List<Diagnostic> withCode(Code code) {
        return diagnostics.stream().filter(diagnostic -> diagnostic.code() == code).toList();
    }
}
