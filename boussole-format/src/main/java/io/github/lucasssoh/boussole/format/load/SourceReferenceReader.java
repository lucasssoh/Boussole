package io.github.lucasssoh.boussole.format.load;

import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCode;
import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCollector;
import io.github.lucasssoh.boussole.format.diagnostic.SourcePosition;
import io.github.lucasssoh.boussole.format.model.SourceReference;
import io.github.lucasssoh.boussole.format.yaml.YamlMap;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Reads the {@code sources} lists of YAML files: {@code file.pdf} or {@code file.pdf#page=12}. */
final class SourceReferenceReader {

    private static final Pattern SOURCE = Pattern.compile("(.+?)(?:#page=(\\d+))?");

    private SourceReferenceReader() {
    }

    static List<SourceReference> read(YamlMap map, String key, DiagnosticCollector diagnostics) {
        List<String> texts = map.texts(key);
        List<SourcePosition> positions = map.textPositions(key);
        List<SourceReference> sources = new ArrayList<>();
        for (int index = 0; index < texts.size(); index++) {
            SourcePosition position = index < positions.size() ? positions.get(index) : map.positionOf(key);
            Matcher matcher = SOURCE.matcher(texts.get(index).strip());
            if (!matcher.matches() || "0".equals(matcher.group(2))) {
                diagnostics.report(DiagnosticCode.F001, position, "a source reads `file.pdf` or `file.pdf#page=12`");
                continue;
            }
            OptionalInt page = matcher.group(2) == null ? OptionalInt.empty() : OptionalInt.of(Integer.parseInt(matcher.group(2)));
            sources.add(new SourceReference(matcher.group(1), page, position));
        }
        return List.copyOf(sources);
    }
}
