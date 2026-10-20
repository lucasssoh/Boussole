package io.github.lucasssoh.boussole.format.parse.directive;

import io.github.lucasssoh.boussole.format.parse.Attributes;
import io.github.lucasssoh.boussole.format.parse.RawNode;
import java.util.List;

/** A directive whose kind, place and attribute names were checked, handed to its parser. */
public record Directive(String name, String label, Attributes attributes, List<RawNode> children, int line) {
}
