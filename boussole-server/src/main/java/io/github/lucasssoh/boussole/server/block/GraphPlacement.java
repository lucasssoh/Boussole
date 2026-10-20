package io.github.lucasssoh.boussole.server.block;

import io.github.lucasssoh.boussole.format.model.block.GraphBlock;
import io.github.lucasssoh.boussole.format.model.block.GraphEdge;
import io.github.lucasssoh.boussole.format.model.block.GraphNode;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Where each node of a graph is drawn, in grid units (one unit between two neighbours), the top
 * left node at 0,0: the author's places when given, else those of the graph's layout.
 */
final class GraphPlacement {

    /** A place in grid units. */
    record Point(double x, double y) {
    }

    private GraphPlacement() {
    }

    static Map<String, Point> place(GraphBlock graph) {
        Map<String, Point> places = graph.placedByAuthor()
                ? authors(graph)
                : switch (graph.layout()) {
                    case ROW -> row(graph.nodes());
                    case CIRCLE -> graph.nodes().size() <= 2 ? row(graph.nodes()) : circle(graph.nodes());
                    case LAYERS -> layers(graph);
                };
        return fromTopLeft(places);
    }

    private static Map<String, Point> authors(GraphBlock graph) {
        Map<String, Point> places = new LinkedHashMap<>();
        graph.nodes().forEach(node -> node.at().ifPresent(at -> places.put(node.id(), new Point(at.x(), at.y()))));
        return places;
    }

    private static Map<String, Point> row(List<GraphNode> nodes) {
        Map<String, Point> places = new LinkedHashMap<>();
        for (int index = 0; index < nodes.size(); index++) {
            places.put(nodes.get(index).id(), new Point(index, 0));
        }
        return places;
    }

    /** On a circle from the top, clockwise, wide enough for one unit between neighbours. */
    private static Map<String, Point> circle(List<GraphNode> nodes) {
        int count = nodes.size();
        double radius = Math.max(1, 0.5 / Math.sin(Math.PI / count));
        Map<String, Point> places = new LinkedHashMap<>();
        for (int index = 0; index < count; index++) {
            double angle = -Math.PI / 2 + 2 * Math.PI * index / count;
            places.put(nodes.get(index).id(), new Point(round(radius * Math.cos(angle)), round(radius * Math.sin(angle))));
        }
        return places;
    }

    /**
     * Columns by distance from the first node, following the arrows (and edges both ways); a node
     * it does not reach starts again from column 0. Each column is centred on the others.
     */
    private static Map<String, Point> layers(GraphBlock graph) {
        Map<String, List<String>> next = new HashMap<>();
        for (GraphEdge edge : graph.edges()) {
            next.computeIfAbsent(edge.from(), unused -> new ArrayList<>()).add(edge.to());
            if (!edge.directed()) {
                next.computeIfAbsent(edge.to(), unused -> new ArrayList<>()).add(edge.from());
            }
        }
        Map<String, Integer> column = new LinkedHashMap<>();
        for (GraphNode start : graph.nodes()) {
            if (column.containsKey(start.id())) {
                continue;
            }
            Deque<String> queue = new ArrayDeque<>(List.of(start.id()));
            column.put(start.id(), 0);
            while (!queue.isEmpty()) {
                String node = queue.poll();
                for (String reached : next.getOrDefault(node, List.of())) {
                    if (!column.containsKey(reached)) {
                        column.put(reached, column.get(node) + 1);
                        queue.add(reached);
                    }
                }
            }
        }
        Map<Integer, List<String>> byColumn = new LinkedHashMap<>();
        graph.nodes().forEach(node -> byColumn.computeIfAbsent(column.get(node.id()), unused -> new ArrayList<>()).add(node.id()));
        Map<String, Point> places = new LinkedHashMap<>();
        byColumn.forEach((x, ids) -> {
            for (int index = 0; index < ids.size(); index++) {
                places.put(ids.get(index), new Point(x, index - (ids.size() - 1) / 2.0));
            }
        });
        return places;
    }

    private static Map<String, Point> fromTopLeft(Map<String, Point> places) {
        double left = places.values().stream().mapToDouble(Point::x).min().orElse(0);
        double top = places.values().stream().mapToDouble(Point::y).min().orElse(0);
        Map<String, Point> moved = new LinkedHashMap<>();
        places.forEach((id, point) -> moved.put(id, new Point(round(point.x() - left), round(point.y() - top))));
        return moved;
    }

    private static double round(double value) {
        return Math.round(value * 1000) / 1000.0;
    }
}
