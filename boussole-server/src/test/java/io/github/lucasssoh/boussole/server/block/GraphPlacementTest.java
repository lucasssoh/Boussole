package io.github.lucasssoh.boussole.server.block;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.lucasssoh.boussole.format.diagnostic.SourcePosition;
import io.github.lucasssoh.boussole.format.model.block.GraphBlock;
import io.github.lucasssoh.boussole.format.model.block.GraphEdge;
import io.github.lucasssoh.boussole.format.model.block.GraphLayout;
import io.github.lucasssoh.boussole.format.model.block.GraphNode;
import io.github.lucasssoh.boussole.format.model.block.GraphPoint;
import io.github.lucasssoh.boussole.server.block.GraphPlacement.Point;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class GraphPlacementTest {

    private static GraphNode node(String id) {
        return new GraphNode(id, Optional.empty(), Optional.empty(), false);
    }

    private static GraphEdge arrow(String from, String to) {
        return new GraphEdge(from, to, true, Optional.empty(), false);
    }

    private static GraphBlock graph(GraphLayout layout, List<GraphNode> nodes, List<GraphEdge> edges) {
        return new GraphBlock("", layout, nodes, edges, SourcePosition.fileOnly("s1.md"));
    }

    @Test
    void layers_follow_the_arrows_from_the_first_node_and_centre_each_column() {
        Map<String, Point> places = GraphPlacement.place(graph(GraphLayout.LAYERS,
                List.of(node("s"), node("a"), node("b"), node("t")),
                List.of(arrow("s", "a"), arrow("s", "b"), arrow("a", "t"), arrow("b", "t"))));

        assertThat(places).containsEntry("s", new Point(0, 0.5))
                .containsEntry("a", new Point(1, 0))
                .containsEntry("b", new Point(1, 1))
                .containsEntry("t", new Point(2, 0.5));
    }

    @Test
    void a_circle_starts_at_the_top_and_the_drawing_starts_at_zero() {
        Map<String, Point> places = GraphPlacement.place(graph(GraphLayout.CIRCLE,
                List.of(node("a"), node("b"), node("c"), node("d")), List.of()));

        assertThat(places.get("a").y()).isZero();
        assertThat(places.values()).allSatisfy(point -> assertThat(point.x()).isGreaterThanOrEqualTo(0));
        assertThat(places.get("a").x()).isEqualTo(places.get("c").x());
    }

    @Test
    void the_authors_places_win_over_the_layout() {
        Map<String, Point> places = GraphPlacement.place(graph(GraphLayout.CIRCLE, List.of(
                new GraphNode("u", Optional.empty(), Optional.of(new GraphPoint(3, 2)), false),
                new GraphNode("v", Optional.empty(), Optional.of(new GraphPoint(5, 2)), false)), List.of()));

        assertThat(places).containsEntry("u", new Point(0, 0)).containsEntry("v", new Point(2, 0));
    }
}
