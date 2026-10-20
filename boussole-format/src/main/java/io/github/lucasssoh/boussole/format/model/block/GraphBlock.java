package io.github.lucasssoh.boussole.format.model.block;

import io.github.lucasssoh.boussole.format.diagnostic.SourcePosition;
import java.util.List;

/**
 * A small graph that Boussole draws itself: nodes and edges written as lines of text, placed by
 * {@code layout} unless every node gives its own place. Nodes are in the order they first appear.
 */
public record GraphBlock(String label, GraphLayout layout, List<GraphNode> nodes, List<GraphEdge> edges, SourcePosition position)
        implements Block {

    public GraphBlock {
        nodes = List.copyOf(nodes);
        edges = List.copyOf(edges);
    }

    /** Every node was given its place with {@code @ x,y}. */
    public boolean placedByAuthor() {
        return nodes.stream().allMatch(node -> node.at().isPresent());
    }
}
