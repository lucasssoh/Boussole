package io.github.lucasssoh.boussole.format.parse;

import java.util.List;
import java.util.Optional;

/**
 * The structure of a session body before the directives are understood: text, level-2 headings,
 * and directives with their nested lines. Built by {@link RawTreeBuilder}.
 */
public sealed interface RawNode {

    int line();

    record Text(List<String> lines, int line) implements RawNode {

        public boolean isBlank() {
            return lines.stream().allMatch(String::isBlank);
        }

        public String markdown() {
            return String.join("\n", lines).strip();
        }
    }

    record Heading(String title, Optional<String> id, int line) implements RawNode {
    }

    record Leaf(DirectiveLine directive, int line) implements RawNode {
    }

    record Container(DirectiveLine directive, List<RawNode> children, int line) implements RawNode {
    }
}
