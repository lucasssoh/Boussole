package io.github.lucasssoh.boussole.format.yaml;

import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCode;
import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCollector;
import io.github.lucasssoh.boussole.format.diagnostic.SourcePosition;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.api.lowlevel.Compose;
import org.snakeyaml.engine.v2.exceptions.Mark;
import org.snakeyaml.engine.v2.exceptions.MarkedYamlEngineException;
import org.snakeyaml.engine.v2.exceptions.YamlEngineException;
import org.snakeyaml.engine.v2.nodes.MappingNode;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.nodes.NodeTuple;
import org.snakeyaml.engine.v2.nodes.ScalarNode;
import org.snakeyaml.engine.v2.nodes.SequenceNode;

/**
 * A YAML mapping that knows where each of its values is, and reports F001 when a value is
 * missing or of the wrong shape. Getters never throw: they report and return empty.
 */
public final class YamlMap {

    private final MappingNode node;
    private final YamlSource source;

    private YamlMap(MappingNode node, YamlSource source) {
        this.node = node;
        this.source = source;
    }

    /** The mapping at the root of {@code text}, or empty after reporting why there is none. */
    public static Optional<YamlMap> parse(String text, YamlSource source) {
        return compose(text, source).flatMap(root -> {
            if (root instanceof MappingNode mapping) {
                return Optional.of(new YamlMap(mapping, source));
            }
            source.report(source.position(root), "the file must hold `key: value` lines");
            return Optional.empty();
        });
    }

    /** The mappings of the sequence at the root of {@code text}; an empty text is an empty list. */
    public static List<YamlMap> parseList(String text, YamlSource source) {
        if (text.isBlank()) {
            return List.of();
        }
        return compose(text, source).map(root -> {
            if (root instanceof SequenceNode sequence) {
                return mappings(sequence, source, "an entry");
            }
            source.report(source.position(root), "the file must be a list of `- key: value` entries");
            return List.<YamlMap>of();
        }).orElse(List.of());
    }

    private static Optional<Node> compose(String text, YamlSource source) {
        try {
            Optional<Node> root = new Compose(LoadSettings.builder().build()).composeString(text);
            if (root.isEmpty()) {
                source.report(source.file(), "the file is empty");
            }
            return root;
        } catch (MarkedYamlEngineException e) {
            SourcePosition position = e.getProblemMark().map(source::position).orElse(source.file());
            source.report(position, "not valid YAML: " + e.getProblem());
            return Optional.empty();
        } catch (YamlEngineException e) {
            source.report(source.file(), "not valid YAML: " + e.getMessage());
            return Optional.empty();
        }
    }

    public SourcePosition position() {
        return source.position(node);
    }

    public SourcePosition positionOf(String key) {
        return value(key).map(source::position).orElse(position());
    }

    /** Reports every key of the mapping that is not in {@code known}, such as a misspelt field. */
    public void reportUnknownKeys(Set<String> known) {
        for (NodeTuple tuple : node.getValue()) {
            if (tuple.getKeyNode() instanceof ScalarNode scalar && !known.contains(scalar.getValue())) {
                source.report(source.position(scalar), "`" + scalar.getValue() + "` is not a field of this file");
            }
        }
    }

    /** {@code true} or {@code false}; reported and empty for anything else. */
    public Optional<Boolean> flag(String key) {
        return text(key).flatMap(text -> switch (text.strip()) {
            case "true" -> Optional.of(true);
            case "false" -> Optional.of(false);
            default -> {
                source.report(positionOf(key), "`" + key + "` is true or false, not `" + text + "`");
                yield Optional.empty();
            }
        });
    }

    public boolean has(String key) {
        return value(key).isPresent();
    }

    public Optional<String> text(String key) {
        return value(key).flatMap(value -> {
            if (value instanceof ScalarNode scalar) {
                return Optional.of(scalar.getValue());
            }
            source.report(source.position(value), "`" + key + "` must be a single value");
            return Optional.empty();
        });
    }

    /** A required text: reported when missing or blank, and then {@code ""}. */
    public String requiredText(String key) {
        Optional<String> text = text(key);
        if (text.isEmpty() || text.get().isBlank()) {
            if (!has(key) || text.isPresent()) {
                source.report(positionOf(key), "`" + key + "` is required");
            }
            return "";
        }
        return text.get();
    }

    public Optional<Integer> integer(String key) {
        return text(key).flatMap(text -> {
            try {
                return Optional.of(Integer.parseInt(text.strip()));
            } catch (NumberFormatException e) {
                source.report(positionOf(key), "`" + key + "` must be a whole number, not `" + text + "`");
                return Optional.empty();
            }
        });
    }

    /** A list of single values; a single value alone counts as a list of one. Missing is empty. */
    public List<String> texts(String key) {
        Optional<Node> value = value(key);
        if (value.isEmpty()) {
            return List.of();
        }
        if (value.get() instanceof ScalarNode scalar) {
            return scalar.getValue().isBlank() ? List.of() : List.of(scalar.getValue());
        }
        if (!(value.get() instanceof SequenceNode sequence)) {
            source.report(source.position(value.get()), "`" + key + "` must be a list");
            return List.of();
        }
        List<String> texts = new ArrayList<>();
        for (Node item : sequence.getValue()) {
            if (item instanceof ScalarNode scalar) {
                texts.add(scalar.getValue());
            } else {
                source.report(source.position(item), "each item of `" + key + "` must be a single value");
            }
        }
        return List.copyOf(texts);
    }

    /** Positions of the items of {@link #texts(String)}, in the same order. */
    public List<SourcePosition> textPositions(String key) {
        return value(key)
                .map(value -> value instanceof SequenceNode sequence
                        ? sequence.getValue().stream().filter(ScalarNode.class::isInstance).map(source::position).toList()
                        : List.of(source.position(value)))
                .orElse(List.of());
    }

    /** A list of mappings. Missing is empty. */
    public List<YamlMap> maps(String key) {
        Optional<Node> value = value(key);
        if (value.isEmpty()) {
            return List.of();
        }
        if (value.get() instanceof SequenceNode sequence) {
            return mappings(sequence, source, "each item of `" + key + "`");
        }
        source.report(source.position(value.get()), "`" + key + "` must be a list");
        return List.of();
    }

    private static List<YamlMap> mappings(SequenceNode sequence, YamlSource source, String what) {
        List<YamlMap> maps = new ArrayList<>();
        for (Node item : sequence.getValue()) {
            if (item instanceof MappingNode mapping) {
                maps.add(new YamlMap(mapping, source));
            } else {
                source.report(source.position(item), what + " must be a mapping of `key: value` lines");
            }
        }
        return List.copyOf(maps);
    }

    private Optional<Node> value(String key) {
        for (NodeTuple tuple : node.getValue()) {
            if (tuple.getKeyNode() instanceof ScalarNode scalar && scalar.getValue().equals(key)) {
                return Optional.of(tuple.getValueNode());
            }
        }
        return Optional.empty();
    }

    /** Where the YAML text comes from: a file, and the line it starts at in that file. */
    public record YamlSource(String fileName, int firstLine, DiagnosticCollector diagnostics) {

        public static YamlSource of(String fileName, DiagnosticCollector diagnostics) {
            return new YamlSource(fileName, 1, diagnostics);
        }

        SourcePosition file() {
            return SourcePosition.fileOnly(fileName);
        }

        SourcePosition position(Node node) {
            return node.getStartMark().map(this::position).orElse(file());
        }

        SourcePosition position(Mark mark) {
            return new SourcePosition(fileName, firstLine + mark.getLine());
        }

        void report(SourcePosition position, String message) {
            diagnostics.report(DiagnosticCode.F001, position, message);
        }
    }
}
