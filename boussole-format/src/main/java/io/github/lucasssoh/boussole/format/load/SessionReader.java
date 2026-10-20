package io.github.lucasssoh.boussole.format.load;

import io.github.lucasssoh.boussole.format.diagnostic.Code;
import io.github.lucasssoh.boussole.format.diagnostic.Diagnostics;
import io.github.lucasssoh.boussole.format.diagnostic.Position;
import io.github.lucasssoh.boussole.format.model.Session;
import io.github.lucasssoh.boussole.format.model.SessionHeader;
import io.github.lucasssoh.boussole.format.parse.SessionBodyParser;
import io.github.lucasssoh.boussole.format.yaml.YamlMap;
import java.util.Arrays;
import java.util.List;

/** Reads a session file: its front matter between {@code ---} lines, then its body. */
public final class SessionReader {

    private static final String FRONT_MATTER = "---";

    private SessionReader() {
    }

    /** @param file the file, relative to the subject folder, for diagnostics */
    public static Session read(String text, String file, Diagnostics diagnostics) {
        List<String> lines = Arrays.asList(text.replace("\r\n", "\n").split("\n", -1));
        int end = frontMatterEnd(lines);
        if (end < 0) {
            diagnostics.report(Code.F001, new Position(file, 1),
                    "a session starts with its front matter, between two `---` lines");
            return new Session(emptyHeader(file), SessionBodyParser.parse(lines, 1, file, diagnostics));
        }
        String yaml = String.join("\n", lines.subList(1, end));
        SessionHeader header = YamlMap.parse(yaml, new YamlMap.YamlSource(file, 2, diagnostics))
                .map(map -> header(map, file, diagnostics))
                .orElseGet(() -> emptyHeader(file));
        return new Session(header, SessionBodyParser.parse(lines.subList(end + 1, lines.size()), end + 2, file, diagnostics));
    }

    /** The index of the line closing the front matter, or -1 when there is no front matter. */
    private static int frontMatterEnd(List<String> lines) {
        if (lines.isEmpty() || !lines.getFirst().strip().equals(FRONT_MATTER)) {
            return -1;
        }
        for (int index = 1; index < lines.size(); index++) {
            if (lines.get(index).strip().equals(FRONT_MATTER)) {
                return index;
            }
        }
        return -1;
    }

    private static SessionHeader header(YamlMap map, String file, Diagnostics diagnostics) {
        List<String> objectives = map.texts("objectives");
        if (objectives.isEmpty()) {
            diagnostics.report(Code.F001, map.positionOf("objectives"), "`objectives` is required: what the learner can do at the end");
        }
        return new SessionHeader(
                map.requiredText("id"),
                objectives,
                map.texts("prerequisites"),
                map.texts("terms"),
                SourceRefs.read(map, "sources", diagnostics),
                new Position(file, 1));
    }

    private static SessionHeader emptyHeader(String file) {
        return new SessionHeader("", List.of(), List.of(), List.of(), List.of(), new Position(file, 1));
    }
}
