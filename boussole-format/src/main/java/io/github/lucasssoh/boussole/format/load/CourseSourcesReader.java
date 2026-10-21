package io.github.lucasssoh.boussole.format.load;

import io.github.lucasssoh.boussole.format.model.SourceProvenance;
import io.github.lucasssoh.boussole.format.parse.InvalidAttributeException;
import io.github.lucasssoh.boussole.format.parse.directive.WebAddress;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCode;
import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCollector;
import io.github.lucasssoh.boussole.format.model.CourseSources;
import io.github.lucasssoh.boussole.format.model.SourceEntry;
import io.github.lucasssoh.boussole.format.model.SourceStatus;
import io.github.lucasssoh.boussole.format.yaml.YamlMap;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/** Reads {@code cursus/sources.yaml}, which a subject may not have yet. */
final class CourseSourcesReader {

    private static final Set<String> FIELDS = Set.of("file", "sha256", "status", "chapters", "note", "url", "title", "licence", "retrieved");

    private CourseSourcesReader() {
    }

    static Optional<CourseSources> read(SubjectFolderFiles files, DiagnosticCollector diagnostics) {
        if (!files.exists(SubjectFolderFiles.SOURCES)) {
            return Optional.empty();
        }
        return files.read(SubjectFolderFiles.SOURCES)
                .map(text -> YamlMap.parseList(text, YamlMap.YamlSource.of(SubjectFolderFiles.SOURCES, diagnostics)))
                .map(entries -> new CourseSources(entries.stream()
                        .map(entry -> entry(entry, diagnostics))
                        .flatMap(Optional::stream)
                        .toList()));
    }

    private static Optional<SourceEntry> entry(YamlMap map, DiagnosticCollector diagnostics) {
        map.reportUnknownKeys(FIELDS);
        String file = map.requiredText("file");
        String written = map.text("status").orElse("used").strip();
        Optional<SourceStatus> status = SourceStatus.fromWritten(written);
        if (status.isEmpty()) {
            diagnostics.report(DiagnosticCode.F001, map.positionOf("status"),
                    "`status` is one of used, ignored, pending, not `" + written + "`");
            return Optional.empty();
        }
        return provenance(map, diagnostics).map(provenance -> new SourceEntry(
                file,
                map.text("sha256").map(value -> value.strip().toLowerCase(Locale.ROOT)),
                status.get(),
                map.texts("chapters"),
                map.text("note").orElse("").strip(),
                provenance,
                map.positionOf("file")));
    }

    private static Optional<SourceProvenance> provenance(YamlMap map, DiagnosticCollector diagnostics) {
        Optional<String> url = map.text("url").map(String::strip);
        if (url.isPresent()) {
            try {
                WebAddress.read(url.get());
            } catch (InvalidAttributeException e) {
                diagnostics.report(DiagnosticCode.F001, map.positionOf("url"), e.getMessage());
                return Optional.empty();
            }
        }
        Optional<String> retrieved = map.text("retrieved").map(String::strip);
        try {
            return Optional.of(new SourceProvenance(url, map.text("title").map(String::strip), map.text("licence").map(String::strip),
                    retrieved.map(LocalDate::parse)));
        } catch (DateTimeParseException e) {
            diagnostics.report(DiagnosticCode.F001, map.positionOf("retrieved"),
                    "`retrieved` is a date written 2026-10-21, not `" + retrieved.orElse("") + "`");
            return Optional.empty();
        }
    }
}
