package io.github.lucasssoh.boussole.format.model.block;

import java.util.Optional;

/** An edge of a graph, an arrow when {@code directed}, with an optional label such as a weight. */
public record GraphEdge(String from, String to, boolean directed, Optional<String> label, boolean emphasized) {
}
