package io.github.lucasssoh.boussole.format.parse.directive;

import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCode;
import io.github.lucasssoh.boussole.format.model.block.Block;
import io.github.lucasssoh.boussole.format.model.block.GraphBlock;
import io.github.lucasssoh.boussole.format.model.block.GraphEdge;
import io.github.lucasssoh.boussole.format.model.block.GraphLayout;
import io.github.lucasssoh.boussole.format.model.block.GraphNode;
import io.github.lucasssoh.boussole.format.model.block.GraphPoint;
import io.github.lucasssoh.boussole.format.parse.InvalidAttributeException;
import io.github.lucasssoh.boussole.format.parse.ParseContext;
import io.github.lucasssoh.boussole.format.parse.RawNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@code :::graph[caption]{layout=circle|row|layers}}: a small graph, one node or edge per line.
 *
 * <pre>
 * s "Source" @ 0,1 !
 * s -> a : 4
 * a -- b
 * </pre>
 *
 * A node line gives a label, a place on the grid and {@code !} to make it stand out, each
 * optional; an edge line is {@code ->} (an arrow) or {@code --}, with an optional label after
 * {@code :} and {@code !}. A node used by an edge needs no line of its own.
 */
final class GraphDirectiveParser implements DirectiveParser {

    private static final DirectiveSpec SPEC =
            new DirectiveSpec("graph", DirectiveKind.CONTAINER, Set.of(), Set.of("layout"), DirectivePlaces.MEDIA);
    /** Above this, a graph no longer reads at a glance: it wants an image, or several graphs. */
    static final int MAX_NODES = 12;
    private static final String ID = "[\\p{L}\\p{N}_']+";
    private static final Pattern EDGE = Pattern.compile("(" + ID + ")\\s*(->|--)\\s*(" + ID + ")(?:\\s*:\\s*(.*?))?");
    private static final Pattern NODE = Pattern.compile("(" + ID + ")(?:\\s+\"([^\"]*)\")?(?:\\s*@\\s*(\\d+)\\s*,\\s*(\\d+))?");
    private static final String SHAPES = "a node (`a \"Label\" @ 0,1`) nor an edge (`a -> b : 3` or `a -- b`), with `!` at the end to make it stand out";

    @Override
    public DirectiveSpec spec() {
        return SPEC;
    }

    @Override
    public Optional<Block> parse(CheckedDirective directive, ParseContext context) {
        GraphLayout layout = directive.attributes().text("layout").map(value -> GraphLayout.fromAttribute(value)
                .orElseThrow(() -> new InvalidAttributeException("layout", "must be circle, row or layers, not `" + value + "`")))
                .orElse(GraphLayout.CIRCLE);
        Reader reader = new Reader(context);
        for (RawNode child : directive.children()) {
            if (child instanceof RawNode.Text text) {
                for (int index = 0; index < text.lines().size(); index++) {
                    reader.read(text.lines().get(index).strip(), text.line() + index);
                }
            } else {
                reader.fail(child.line(), "`graph` holds only lines of nodes and edges");
            }
        }
        return reader.graph(directive, layout, context);
    }

    /** Reads the lines of one graph, reporting what it cannot use. */
    private static final class Reader {

        private final ParseContext context;
        private final Map<String, GraphNode> nodes = new LinkedHashMap<>();
        private final Map<String, Boolean> declared = new HashMap<>();
        private final List<GraphEdge> edges = new ArrayList<>();
        private boolean failed;

        Reader(ParseContext context) {
            this.context = context;
        }

        void read(String line, int number) {
            if (line.isEmpty()) {
                return;
            }
            boolean emphasized = line.equals("!") || line.endsWith(" !");
            String content = emphasized ? line.substring(0, line.length() - 1).strip() : line;
            Matcher edge = EDGE.matcher(content);
            Matcher node = NODE.matcher(content);
            if (edge.matches()) {
                edge(edge, emphasized, number);
            } else if (node.matches()) {
                node(node, emphasized, number);
            } else {
                fail(number, "`" + line + "` is neither " + SHAPES);
            }
        }

        private void edge(Matcher edge, boolean emphasized, int number) {
            String from = edge.group(1);
            String to = edge.group(3);
            if (from.equals(to)) {
                fail(number, "`" + from + "` is linked to itself: a loop is not drawn, say it in the text");
                return;
            }
            nodes.putIfAbsent(from, new GraphNode(from, Optional.empty(), Optional.empty(), false));
            nodes.putIfAbsent(to, new GraphNode(to, Optional.empty(), Optional.empty(), false));
            Optional<String> label = Optional.ofNullable(edge.group(4)).map(String::strip).filter(text -> !text.isEmpty());
            edges.add(new GraphEdge(from, to, edge.group(2).equals("->"), label, emphasized));
        }

        private void node(Matcher node, boolean emphasized, int number) {
            String id = node.group(1);
            if (declared.put(id, true) != null) {
                fail(number, "node `" + id + "` is given twice: write it on one line");
                return;
            }
            Optional<GraphPoint> at = node.group(3) == null
                    ? Optional.empty()
                    : Optional.of(new GraphPoint(Integer.parseInt(node.group(3)), Integer.parseInt(node.group(4))));
            GraphNode read = new GraphNode(id, Optional.ofNullable(node.group(2)).filter(text -> !text.isBlank()), at, emphasized);
            nodes.put(id, read);
        }

        void fail(int line, String message) {
            failed = true;
            context.report(DiagnosticCode.F037, line, message);
        }

        Optional<Block> graph(CheckedDirective directive, GraphLayout layout, ParseContext context) {
            int line = directive.line();
            if (nodes.isEmpty() && !failed) {
                fail(line, "`graph` is empty: write one node or edge per line, such as `a -> b : 3`");
            }
            long placed = nodes.values().stream().filter(node -> node.at().isPresent()).count();
            if (placed > 0 && placed < nodes.size()) {
                List<String> missing = nodes.values().stream().filter(node -> node.at().isEmpty()).map(GraphNode::id).toList();
                fail(line, "either every node gives its place with `@ x,y`, or none does; missing: " + String.join(", ", missing));
            }
            Map<GraphPoint, String> taken = new HashMap<>();
            for (GraphNode node : nodes.values()) {
                node.at().ifPresent(point -> {
                    String other = taken.putIfAbsent(point, node.id());
                    if (other != null) {
                        fail(line, "`" + other + "` and `" + node.id() + "` are both at " + point.x() + "," + point.y());
                    }
                });
            }
            if (nodes.size() > MAX_NODES) {
                context.report(DiagnosticCode.P011, line, "this graph has " + nodes.size() + " nodes; up to " + MAX_NODES
                        + " read at a glance: split it, or draw it as an image");
            }
            return failed
                    ? Optional.empty()
                    : Optional.of(new GraphBlock(directive.label(), layout, List.copyOf(nodes.values()), edges, context.position(line)));
        }
    }
}
