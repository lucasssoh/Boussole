package io.github.lucasssoh.boussole.server.block;

import io.github.lucasssoh.boussole.core.learn.check.Verdict;
import io.github.lucasssoh.boussole.core.learn.progress.ExerciseProgress;
import io.github.lucasssoh.boussole.format.model.answer.Answer;
import io.github.lucasssoh.boussole.format.model.answer.FillAnswer;
import io.github.lucasssoh.boussole.format.model.answer.FillGap;
import io.github.lucasssoh.boussole.format.model.answer.MatchAnswer;
import io.github.lucasssoh.boussole.format.model.answer.MatchPair;
import io.github.lucasssoh.boussole.format.model.answer.OrderAnswer;
import io.github.lucasssoh.boussole.format.model.answer.SingleAnswer;
import io.github.lucasssoh.boussole.format.model.block.Block;
import io.github.lucasssoh.boussole.format.model.block.CalloutBlock;
import io.github.lucasssoh.boussole.format.model.block.ExampleBlock;
import io.github.lucasssoh.boussole.format.model.block.ExerciseBlock;
import io.github.lucasssoh.boussole.format.model.block.ExerciseHint;
import io.github.lucasssoh.boussole.format.model.block.ExerciseRefBlock;
import io.github.lucasssoh.boussole.format.model.block.GraphBlock;
import io.github.lucasssoh.boussole.format.model.block.ExerciseType;
import io.github.lucasssoh.boussole.format.model.block.McqBlock;
import io.github.lucasssoh.boussole.format.model.block.McqChoice;
import io.github.lucasssoh.boussole.format.model.block.PdfBlock;
import io.github.lucasssoh.boussole.format.model.block.ProseBlock;
import io.github.lucasssoh.boussole.format.model.block.QuestionTimeEstimator;
import io.github.lucasssoh.boussole.format.model.block.RecallBlock;
import io.github.lucasssoh.boussole.format.model.block.VideoBlock;
import io.github.lucasssoh.boussole.format.model.expectation.ChoiceExpectation;
import io.github.lucasssoh.boussole.format.model.expectation.Expectation;
import io.github.lucasssoh.boussole.format.model.expectation.NumberExpectation;
import io.github.lucasssoh.boussole.format.model.expectation.TextExpectation;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Turns the blocks of a session into what the interface receives. Questions get the learner's
 * progress and the hints already revealed, and never their answer.
 */
public final class BlockViewMapper {

    private final String sourcesDir;
    private final Map<String, Block> referenced;
    private final Map<String, ExerciseProgress> progress;
    private final Map<String, Verdict> answered;

    /** {@code answered}: the questions already answered in the session, whose answer is final. */
    public BlockViewMapper(
            String sourcesDir, Map<String, Block> referenced, Map<String, ExerciseProgress> progress, Map<String, Verdict> answered) {
        this.sourcesDir = sourcesDir;
        this.referenced = referenced;
        this.progress = progress;
        this.answered = answered;
    }

    /** A mapper for content outside a session, such as a hint or a solution: it holds no question. */
    public static BlockViewMapper forContent(String sourcesDir) {
        return new BlockViewMapper(sourcesDir, Map.of(), Map.of(), Map.of());
    }

    public List<BlockView> blocks(List<Block> blocks) {
        return blocks.stream().map(this::block).toList();
    }

    public BlockView block(Block block) {
        return switch (block) {
            case ProseBlock prose -> new BlockView.Prose(prose.markdown());
            case ExampleBlock example -> new BlockView.Example(example.label(), blocks(example.content()));
            case RecallBlock recall -> new BlockView.Recall(blocks(recall.content()));
            case CalloutBlock callout -> new BlockView.Callout(callout.kind().name().toLowerCase(Locale.ROOT), blocks(callout.content()));
            case VideoBlock video -> new BlockView.Video(video.label(), video.youtubeId(),
                    video.start().map(Duration::toSeconds).orElse(null), video.end().map(Duration::toSeconds).orElse(null));
            case PdfBlock pdf -> new BlockView.Pdf(pdf.label(), SourcePaths.of(sourcesDir, pdf.source().file()),
                    pdf.source().page().isPresent() ? pdf.source().page().getAsInt() : null);
            case GraphBlock graph -> graph(graph);
            case McqBlock mcq -> mcq(mcq, false);
            case ExerciseBlock exercise -> exercise(exercise, false);
            case ExerciseRefBlock ref -> reused(ref);
        };
    }

    private static BlockView.Graph graph(GraphBlock graph) {
        Map<String, GraphPlacement.Point> places = GraphPlacement.place(graph);
        List<BlockView.GraphNodeView> nodes = graph.nodes().stream().map(node -> {
            GraphPlacement.Point at = places.get(node.id());
            return new BlockView.GraphNodeView(node.id(), node.label().orElse(node.id()), at.x(), at.y(), node.emphasized());
        }).toList();
        List<BlockView.GraphEdgeView> edges = graph.edges().stream()
                .map(edge -> new BlockView.GraphEdgeView(edge.from(), edge.to(), edge.directed(), edge.label().orElse(null), edge.emphasized()))
                .toList();
        double width = places.values().stream().mapToDouble(GraphPlacement.Point::x).max().orElse(0);
        double height = places.values().stream().mapToDouble(GraphPlacement.Point::y).max().orElse(0);
        return new BlockView.Graph(graph.label(), nodes, edges, width, height);
    }

    private BlockView reused(ExerciseRefBlock ref) {
        return switch (referenced.get(ref.id())) {
            case McqBlock mcq -> mcq(mcq, true);
            case ExerciseBlock exercise -> exercise(exercise, true);
            case null, default -> new BlockView.MissingRef(ref.id());
        };
    }

    private BlockView.Mcq mcq(McqBlock mcq, boolean reused) {
        return new BlockView.Mcq(mcq.id(), mcq.multiple(), blocks(mcq.statement()),
                mcq.choices().stream().map(McqChoice::text).toList(),
                QuestionTimeEstimator.minutes(mcq), hints(mcq.id(), mcq.hints()), progressOf(mcq.id()), reused);
    }

    private BlockView.Exercise exercise(ExerciseBlock exercise, boolean reused) {
        return new BlockView.Exercise(exercise.id(), exercise.type().attributeValue(), blocks(exercise.statement()),
                input(exercise), QuestionTimeEstimator.minutes(exercise), hints(exercise.id(), exercise.hints()),
                progressOf(exercise.id()), reused);
    }

    private HintsView hints(String id, List<ExerciseHint> hints) {
        int revealed = Math.min(progress.getOrDefault(id, ExerciseProgress.NONE).hintsRevealed(), hints.size());
        return new HintsView(hints.size(), hints.subList(0, revealed).stream().map(hint -> blocks(hint.content())).toList());
    }

    private ProgressView progressOf(String id) {
        return ProgressView.of(progress.getOrDefault(id, ExerciseProgress.NONE), Optional.ofNullable(answered.get(id)));
    }

    private static InputView input(ExerciseBlock exercise) {
        if (exercise.type() == ExerciseType.FREE) {
            return new InputView.Free();
        }
        if (exercise.answer().isEmpty()) {
            return new InputView.Unreadable();
        }
        Answer answer = exercise.answer().get();
        return switch (answer) {
            case SingleAnswer single -> switch (single.expected()) {
                case NumberExpectation number -> new InputView.Number(number.unit().orElse(null));
                case TextExpectation _, ChoiceExpectation _ -> new InputView.Text();
            };
            case FillAnswer fill -> new InputView.Fill(fill.gaps().stream().map(gap -> gap(exercise.id(), gap)).toList());
            case OrderAnswer order -> new InputView.Order(SeededShuffle.of(order.items(), exercise.id()));
            case MatchAnswer match -> new InputView.Match(
                    match.pairs().stream().map(MatchPair::left).toList(),
                    SeededShuffle.of(match.pairs().stream().map(MatchPair::right).toList(), exercise.id()));
        };
    }

    private static InputView.Gap gap(String exerciseId, FillGap gap) {
        Expectation expected = gap.expected();
        return switch (expected) {
            case TextExpectation _ -> new InputView.Gap(gap.number(), "text", null, null);
            case NumberExpectation number -> new InputView.Gap(gap.number(), "number", null, number.unit().orElse(null));
            case ChoiceExpectation choice -> new InputView.Gap(gap.number(), "choice",
                    SeededShuffle.of(choice.options(), exerciseId + "#" + gap.number()), null);
        };
    }
}
