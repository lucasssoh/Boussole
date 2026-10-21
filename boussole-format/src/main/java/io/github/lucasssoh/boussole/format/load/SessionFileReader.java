package io.github.lucasssoh.boussole.format.load;

import io.github.lucasssoh.boussole.format.model.Authorship;
import io.github.lucasssoh.boussole.format.model.AuthorshipRole;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Optional;
import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCode;
import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCollector;
import io.github.lucasssoh.boussole.format.diagnostic.SourcePosition;
import io.github.lucasssoh.boussole.format.model.SessionContent;
import io.github.lucasssoh.boussole.format.model.SessionFrontMatter;
import io.github.lucasssoh.boussole.format.parse.SessionBodyParser;
import io.github.lucasssoh.boussole.format.yaml.YamlMap;
import java.util.Arrays;
import java.util.List;

/** Reads a session file: its front matter between {@code ---} lines, then its body. */
public final class SessionFileReader {

    private static final String FRONT_MATTER = "---";

    private SessionFileReader() {
    }

    /** @param file the file, relative to the subject folder, for diagnostics */
    public static SessionContent read(String text, String file, DiagnosticCollector diagnostics) {
        List<String> lines = Arrays.asList(text.replace("\r\n", "\n").split("\n", -1));
        int end = frontMatterEnd(lines);
        if (end < 0) {
            diagnostics.report(DiagnosticCode.F001, new SourcePosition(file, 1),
                    "a session starts with its front matter, between two `---` lines");
            return new SessionContent(emptyHeader(file), SessionBodyParser.parse(lines, 1, file, diagnostics));
        }
        String yaml = String.join("\n", lines.subList(1, end));
        SessionFrontMatter header = YamlMap.parse(yaml, new YamlMap.YamlSource(file, 2, diagnostics))
                .map(map -> header(map, file, diagnostics))
                .orElseGet(() -> emptyHeader(file));
        return new SessionContent(header, SessionBodyParser.parse(lines.subList(end + 1, lines.size()), end + 2, file, diagnostics));
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

    private static SessionFrontMatter header(YamlMap map, String file, DiagnosticCollector diagnostics) {
        List<String> objectives = map.texts("objectives");
        if (objectives.isEmpty()) {
            diagnostics.report(DiagnosticCode.F001, map.positionOf("objectives"), "`objectives` is required: what the learner can do at the end");
        }
        return new SessionFrontMatter(
                map.requiredText("id"),
                objectives,
                map.texts("prerequisites"),
                map.texts("terms"),
                SourceReferenceReader.read(map, "sources", diagnostics),
                writtenBy(map, diagnostics),
                new SourcePosition(file, 1));
    }

    /** {@code written_by}: each line a {@code model} (and its {@code tool}) or a {@code name}, a {@code date}, a {@code role}. */
    private static List<Authorship> writtenBy(YamlMap map, DiagnosticCollector diagnostics) {
        List<Authorship> found = new ArrayList<>();
        for (YamlMap line : map.maps("written_by")) {
            Optional<String> model = line.text("model").map(String::strip).filter(text -> !text.isEmpty());
            Optional<String> name = line.text("name").map(String::strip).filter(text -> !text.isEmpty());
            if (model.isPresent() == name.isPresent()) {
                diagnostics.report(DiagnosticCode.F001, line.position(),
                        "each line of `written_by` has a `model` (an AI, by its exact ID) or a `name` (a person), one of them");
                continue;
            }
            String written = line.text("role").orElse("wrote").strip();
            Optional<AuthorshipRole> role = AuthorshipRole.fromAttribute(written);
            if (role.isEmpty()) {
                diagnostics.report(DiagnosticCode.F001, line.positionOf("role"), "`role` is wrote, revised or translated, not `" + written + "`");
                continue;
            }
            Optional<String> date = line.text("date").map(String::strip);
            try {
                found.add(new Authorship(model, line.text("tool").map(String::strip), name, date.map(LocalDate::parse), role.get()));
            } catch (DateTimeParseException e) {
                diagnostics.report(DiagnosticCode.F001, line.positionOf("date"), "`date` is written 2026-10-21, not `" + date.orElse("") + "`");
            }
        }
        return List.copyOf(found);
    }

    private static SessionFrontMatter emptyHeader(String file) {
        return new SessionFrontMatter("", List.of(), List.of(), List.of(), List.of(), List.of(), new SourcePosition(file, 1));
    }
}
