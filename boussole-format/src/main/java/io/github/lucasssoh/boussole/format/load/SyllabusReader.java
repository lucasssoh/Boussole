package io.github.lucasssoh.boussole.format.load;

import io.github.lucasssoh.boussole.format.diagnostic.Code;
import io.github.lucasssoh.boussole.format.diagnostic.Diagnostics;
import io.github.lucasssoh.boussole.format.model.Chapter;
import io.github.lucasssoh.boussole.format.model.SessionEntry;
import io.github.lucasssoh.boussole.format.model.Syllabus;
import io.github.lucasssoh.boussole.format.model.SyllabusStatus;
import io.github.lucasssoh.boussole.format.yaml.YamlMap;
import java.util.Locale;

/** Reads {@code cursus/syllabus.yaml}. */
final class SyllabusReader {

    static final int MIN_DURATION = 15;
    static final int MAX_DURATION = 120;

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

    private static SessionEntry session(YamlMap map, Diagnostics diagnostics) {
        if (!map.has("duration")) {
            diagnostics.report(Code.F001, map.position(), "`duration` is required, in minutes");
        }
        int duration = map.integer("duration").orElse(0);
        if (map.has("duration") && (duration < MIN_DURATION || duration > MAX_DURATION)) {
            diagnostics.report(Code.F001, map.positionOf("duration"),
                    "`duration` is between " + MIN_DURATION + " and " + MAX_DURATION + " minutes, not " + duration);
        }
        return new SessionEntry(
                map.requiredText("id"),
                map.requiredText("title"),
                duration,
                map.requiredText("goal"),
                map.requiredText("file"),
                map.positionOf("id"));
    }
}
