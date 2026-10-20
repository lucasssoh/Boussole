package io.github.lucasssoh.boussole.format.load;

import io.github.lucasssoh.boussole.format.diagnostic.Code;
import io.github.lucasssoh.boussole.format.diagnostic.Diagnostics;
import io.github.lucasssoh.boussole.format.diagnostic.Position;
import io.github.lucasssoh.boussole.format.model.SourceRef;
import io.github.lucasssoh.boussole.format.yaml.YamlMap;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Reads the {@code sources} lists of YAML files: {@code file.pdf} or {@code file.pdf#page=12}. */
final class SourceRefs {

    private static final Pattern SOURCE = Pattern.compile("(.+?)(?:#page=(\\d+))?");

    private SourceRefs() {
    }

    static List<SourceRef> read(YamlMap map, String key, Diagnostics diagnostics) {
        List<String> texts = map.texts(key);
        List<Position> positions = map.textPositions(key);
        List<SourceRef> sources = new ArrayList<>();
        for (int index = 0; index < texts.size(); index++) {
            Position position = index < positions.size() ? positions.get(index) : map.positionOf(key);
            Matcher matcher = SOURCE.matcher(texts.get(index).strip());
            if (!matcher.matches() || "0".equals(matcher.group(2))) {
                diagnostics.report(Code.F001, position, "a source reads `file.pdf` or `file.pdf#page=12`");
                continue;
            }
            OptionalInt page = matcher.group(2) == null ? OptionalInt.empty() : OptionalInt.of(Integer.parseInt(matcher.group(2)));
            sources.add(new SourceRef(matcher.group(1), page, position));
        }
        return List.copyOf(sources);
    }
}
