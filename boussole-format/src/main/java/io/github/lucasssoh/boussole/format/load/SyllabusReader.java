package io.github.lucasssoh.boussole.format.load;

import io.github.lucasssoh.boussole.format.diagnostic.Code;
import io.github.lucasssoh.boussole.format.diagnostic.Diagnostics;
import io.github.lucasssoh.boussole.format.model.Chapter;
import io.github.lucasssoh.boussole.format.model.SessionEntry;
import io.github.lucasssoh.boussole.format.model.SessionKind;
import io.github.lucasssoh.boussole.format.model.Syllabus;
import io.github.lucasssoh.boussole.format.model.SyllabusStatus;
import io.github.lucasssoh.boussole.format.yaml.YamlMap;
import java.util.Locale;

/** Reads {@code cursus/syllabus.yaml}. */
final class SyllabusReader {

    private SyllabusReader() {
    }

    static Syllabus read(SubjectFiles files, Diagnostics diagnostics) {
        return files.read(SubjectFiles.SYLLABUS)
                .flatMap(text -> YamlMap.parse(text, YamlMap.YamlSource.of(SubjectFiles.SYLLABUS, diagnostics)))
                .map(map -> new Syllabus(
                        status(map, diagnostics),
                        map.maps("chapters").stream().map(chapter -> chapter(chapter, diagnostics)).toList()))
                .orElse(Syllabus.empty());
    }

    private static SyllabusStatus status(YamlMap map, Diagnostics diagnostics) {
        String status = map.requiredText("status");
        return switch (status.toLowerCase(Locale.ROOT)) {
            case "approved" -> SyllabusStatus.APPROVED;
            case "proposed" -> SyllabusStatus.PROPOSED;
            default -> {
                if (!status.isEmpty()) {
                    diagnostics.report(Code.F001, map.positionOf("status"), "`status` is `proposed` or `approved`, not `" + status + "`");
                }
                yield SyllabusStatus.PROPOSED;
            }
        };
    }

    private static Chapter chapter(YamlMap map, Diagnostics diagnostics) {
        return new Chapter(
                map.requiredText("id"),
                map.requiredText("title"),
                map.requiredText("dir"),
                SourceRefs.read(map, "sources", diagnostics),
                map.maps("sessions").stream().map(session -> session(session, diagnostics)).toList(),
                map.positionOf("id"));
    }

    private static SessionKind kind(YamlMap map, Diagnostics diagnostics) {
        String kind = map.text("kind").orElse("lesson").strip();
        return switch (kind) {
            case "lesson" -> SessionKind.LESSON;
            case "review" -> SessionKind.REVIEW;
            case "exam" -> SessionKind.EXAM;
            default -> {
                diagnostics.report(Code.F001, map.positionOf("kind"), "`kind` is lesson, review or exam, not `" + kind + "`");
                yield SessionKind.LESSON;
            }
        };
    }

    private static SessionEntry session(YamlMap map, Diagnostics diagnostics) {
        if (!map.has("duration")) {
            diagnostics.report(Code.F001, map.position(), "`duration` is required, in minutes");
        }
        SessionKind kind = kind(map, diagnostics);
        int duration = map.integer("duration").orElse(0);
        if (map.has("duration") && (duration < kind.minDuration() || duration > kind.maxDuration())) {
            diagnostics.report(Code.F001, map.positionOf("duration"), "`duration` of a "
                    + kind.name().toLowerCase(Locale.ROOT) + " is between " + kind.minDuration() + " and "
                    + kind.maxDuration() + " minutes, not " + duration);
        }
        return new SessionEntry(
                map.requiredText("id"),
                map.requiredText("title"),
                kind,
                duration,
                map.requiredText("goal"),
                map.requiredText("file"),
                map.positionOf("id"));
    }
}
