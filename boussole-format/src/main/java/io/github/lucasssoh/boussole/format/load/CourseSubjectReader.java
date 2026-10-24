package io.github.lucasssoh.boussole.format.load;

import io.github.lucasssoh.boussole.format.model.SubjectCredit;
import java.util.ArrayList;
import java.util.List;
import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCode;
import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCollector;
import io.github.lucasssoh.boussole.format.model.CourseSubject;
import io.github.lucasssoh.boussole.format.model.Domain;
import io.github.lucasssoh.boussole.format.model.TrackLevel;
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
                credits(map, "authors", diagnostics),
                credits(map, "contributors", diagnostics),
                map.text("level").map(String::strip).flatMap(id -> known(TrackLevel.of(id), map, "level", id, diagnostics,
                        "`level` is discovery, secondary, bachelor, master or research, not `" + id + "`")),
                map.text("domain").map(String::strip).flatMap(id -> known(Domain.of(id), map, "domain", id, diagnostics,
                        "`domain` `" + id + "` is not in the list of docs/FORMAT.md §2 (such as information-technology, mathematics, philosophy-and-ethics)")),
                map.positionOf("id")));
    }

    private static <T> Optional<T> known(Optional<T> value, YamlMap map, String key, String id, DiagnosticCollector diagnostics, String message) {
        if (value.isEmpty()) {
            diagnostics.report(DiagnosticCode.F001, map.positionOf(key), message);
        }
        return value;
    }

    /** {@code authors} or {@code contributors}: each a {@code name}, and a {@code role}, {@code url}, {@code organisation: true}. */
    private static List<SubjectCredit> credits(YamlMap map, String key, DiagnosticCollector diagnostics) {
        List<SubjectCredit> credits = new ArrayList<>();
        for (YamlMap credit : map.maps(key)) {
            String name = credit.requiredText("name");
            if (!name.isBlank()) {
                credits.add(new SubjectCredit(name.strip(), credit.flag("organisation").orElse(false),
                        credit.text("role").map(String::strip), credit.text("url").map(String::strip)));
            }
        }
        return List.copyOf(credits);
    }
}
