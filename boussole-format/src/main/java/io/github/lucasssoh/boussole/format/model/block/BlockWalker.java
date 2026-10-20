package io.github.lucasssoh.boussole.format.model.block;

import java.util.List;
import java.util.stream.Stream;

/** Walks nested blocks. */
public final class BlockWalker {

    private BlockWalker() {
    }

    /** Every block of {@code blocks} and every block nested in them, in reading order. */
    public static Stream<Block> walk(List<Block> blocks) {
        return blocks.stream().flatMap(block -> Stream.concat(Stream.of(block), walk(children(block))));
    }

    private static List<Block> children(Block block) {
        return switch (block) {
            case ExampleBlock example -> example.content();
            case RecallBlock recall -> recall.content();
            case CalloutBlock callout -> callout.content();
            case ExerciseBlock exercise -> concat(exercise.statement(), exercise.hints(), exercise.solution().stream().toList());
            case McqBlock mcq -> concat(mcq.statement(), mcq.hints(), mcq.solution().stream().toList());
            case ProseBlock _, VideoBlock _, PdfBlock _, ExerciseRefBlock _ -> List.of();
        };
    }

    private static List<Block> concat(List<Block> statement, List<ExerciseHint> hints, List<ExerciseSolution> solution) {
        return Stream.of(
                        statement.stream(),
                        hints.stream().flatMap(hint -> hint.content().stream()),
                        solution.stream().flatMap(found -> found.content().stream()))
                .flatMap(stream -> stream)
                .toList();
    }
}
