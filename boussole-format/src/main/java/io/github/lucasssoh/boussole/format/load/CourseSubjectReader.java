package io.github.lucasssoh.boussole.format.load;

import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCode;
import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCollector;
import io.github.lucasssoh.boussole.format.model.CourseSubject;
import io.github.lucasssoh.boussole.format.yaml.YamlMap;
import java.util.Optional;

/** Reads {@code subject.yaml}. Without it, or with an unknown format, nothing else is read. */
final class CourseSubjectReader {

    static final int FORMAT = 1;

    private CourseSubjectReader() {
    }

    static Optional<CourseSubject> read(SubjectFolderFiles files, DiagnosticCollector diagnostics) {
        return files.read(SubjectFolderFiles.SUBJECT)
                .flatMap(text -> YamlMap.parse(text, YamlMap.YamlSource.of(SubjectFolderFiles.SUBJECT, diagnostics)))
                .flatMap(map -> subject(map, diagnostics));
    }

    private static Optional<CourseSubject> subject(YamlMap map, DiagnosticCollector diagnostics) {
        if (!map.has("format")) {
            diagnostics.report(DiagnosticCode.F001, map.position(), "`format` is required: `format: " + FORMAT + "`");
            return Optional.empty();
        }
        Optional<Integer> format = map.integer("format");
        if (format.isEmpty()) {
            return Optional.empty();
        }
        if (format.get() != FORMAT) {
            diagnostics.report(DiagnosticCode.F002, map.positionOf("format"),
                    "format " + format.get() + " is unknown: this Boussole reads format " + FORMAT);
            return Optional.empty();
        }
        return Optional.of(new CourseSubject(
                map.requiredText("id"),
                map.requiredText("name"),
                map.requiredText("lang"),
                map.text("sources").orElse("sources"),
                map.text("description"),
                map.positionOf("id")));
    }
}
