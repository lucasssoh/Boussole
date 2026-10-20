package io.github.lucasssoh.boussole.format.load;

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

    private static final Set<String> FIELDS = Set.of("file", "sha256", "status", "chapters", "note");

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
        return Optional.of(new SourceEntry(
                file,
                map.text("sha256").map(value -> value.strip().toLowerCase(Locale.ROOT)),
                status.get(),
                map.texts("chapters"),
                map.text("note").orElse("").strip(),
                map.positionOf("file")));
    }
}
