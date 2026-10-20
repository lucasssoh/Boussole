package io.github.lucasssoh.boussole.format.model.block;

import java.util.Optional;

/** A node of a graph: its ID (drawn when it has no label), its place on a grid, and whether it stands out. */
public record GraphNode(String id, Optional<String> label, Optional<GraphPoint> at, boolean emphasized) {
}
