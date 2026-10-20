package io.github.lucasssoh.boussole.server.block;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.List;

/**
 * A block as the interface receives it. A question never carries its answer, its hints not yet
 * revealed, or its solution: they are asked for separately, when the learner may see them.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = BlockView.Prose.class, name = "prose"),
    @JsonSubTypes.Type(value = BlockView.Example.class, name = "example"),
    @JsonSubTypes.Type(value = BlockView.Recall.class, name = "recall"),
    @JsonSubTypes.Type(value = BlockView.Callout.class, name = "callout"),
    @JsonSubTypes.Type(value = BlockView.Video.class, name = "video"),
    @JsonSubTypes.Type(value = BlockView.Pdf.class, name = "pdf"),
    @JsonSubTypes.Type(value = BlockView.Graph.class, name = "graph"),
    @JsonSubTypes.Type(value = BlockView.Mcq.class, name = "mcq"),
    @JsonSubTypes.Type(value = BlockView.Exercise.class, name = "exercise"),
    @JsonSubTypes.Type(value = BlockView.MissingRef.class, name = "missingRef")
})
public sealed interface BlockView {

    /** Markdown, rendered by the interface. */
    record Prose(String markdown) implements BlockView {
    }

    record Example(String label, List<BlockView> blocks) implements BlockView {
    }

    record Recall(List<BlockView> blocks) implements BlockView {
    }

    /** {@code kind} is {@code note}, {@code tip} or {@code warning}. */
    record Callout(String kind, List<BlockView> blocks) implements BlockView {
    }

    /** {@code start} and {@code end} in seconds, or null for the whole video. */
    record Video(String label, String youtubeId, Long start, Long end) implements BlockView {
    }

    /** {@code path} is relative to the subject folder; {@code page} is null for the first page. */
    record Pdf(String label, String path, Integer page) implements BlockView {
    }

    /**
     * A graph to draw: each node at its place in grid units, {@code width} and {@code height} the
     * extent of those places; {@code text} is what a node shows, its label or else its ID.
     */
    record Graph(String label, List<GraphNodeView> nodes, List<GraphEdgeView> edges, double width, double height)
            implements BlockView {
    }

    record GraphNodeView(String id, String text, double x, double y, boolean emphasized) {
    }

    /** {@code label} is null when the edge has none. */
    record GraphEdgeView(String from, String to, boolean directed, String label, boolean emphasized) {
    }

    /** {@code reused} when the session asks again a question of another session with {@code ::ref}. */
    record Mcq(
            String id,
            boolean multiple,
            List<BlockView> statement,
            List<String> choices,
            double minutes,
            HintsView hints,
            ProgressView progress,
            boolean reused)
            implements BlockView {
    }

    record Exercise(
            String id,
            String exerciseType,
            List<BlockView> statement,
            InputView input,
            double minutes,
            HintsView hints,
            ProgressView progress,
            boolean reused)
            implements BlockView {
    }

    /** A {@code ::ref} to an exercise the subject does not have (the validator reports it). */
    record MissingRef(String id) implements BlockView {
    }
}
