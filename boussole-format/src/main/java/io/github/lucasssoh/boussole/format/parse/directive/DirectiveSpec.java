package io.github.lucasssoh.boussole.format.parse.directive;

import java.util.Set;

/** What a directive looks like: its name, kind, attributes, and the directives it may appear in. */
public record DirectiveSpec(
        String name, DirectiveKind kind, Set<String> required, Set<String> optional, Set<String> parents) {
}
