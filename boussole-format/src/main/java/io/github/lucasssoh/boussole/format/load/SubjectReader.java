package io.github.lucasssoh.boussole.format.load;

import io.github.lucasssoh.boussole.format.diagnostic.Code;
import io.github.lucasssoh.boussole.format.diagnostic.Diagnostics;
import io.github.lucasssoh.boussole.format.model.Subject;
import io.github.lucasssoh.boussole.format.yaml.YamlMap;
import java.util.Optional;

/** Reads {@code subject.yaml}. Without it, or with an unknown format, nothing else is read. */
final class SubjectReader {

    static final int FORMAT = 1;

    private SubjectReader() {
    }

    static Optional<Subject> read(SubjectFiles files, Diagnostics diagnostics) {
        return files.read(SubjectFiles.SUBJECT)
                .flatMap(text -> YamlMap.parse(text, YamlMap.YamlSource.of(SubjectFiles.SUBJECT, diagnostics)))
                .flatMap(map -> subject(map, diagnostics));
    }

    private static Optional<Subject> subject(YamlMap map, Diagnostics diagnostics) {
        if (!map.has("format")) {
            diagnostics.report(Code.F001, map.position(), "`format` is required: `format: " + FORMAT + "`");
            return Optional.empty();
        }
        Optional<Integer> format = map.integer("format");
        if (format.isEmpty()) {
            return Optional.empty();
        }
        if (format.get() != FORMAT) {
            diagnostics.report(Code.F002, map.positionOf("format"),
                    "format " + format.get() + " is unknown: this Boussole reads format " + FORMAT);
            return Optional.empty();
        }
        return Optional.of(new Subject(
                map.requiredText("id"),
                map.requiredText("name"),
                map.requiredText("lang"),
                map.text("sources").orElse("sources"),
                map.text("description"),
                map.positionOf("id")));
    }
}
