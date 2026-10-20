package io.github.lucasssoh.boussole.format.model.block;

import java.util.List;
import java.util.stream.Stream;

/** Walks nested blocks. */
public final class Blocks {

    private Blocks() {
    }

    /** Every block of {@code blocks} and every block nested in them, in reading order. */
    public static Stream<Block> walk(List<Block> blocks) {
        return blocks.stream().flatMap(block -> Stream.concat(Stream.of(block), walk(children(block))));
    }

    private static List<Block> children(Block block) {
        return switch (block) {
            case Example example -> example.content();
            case Recall recall -> recall.content();
            case Callout callout -> callout.content();
            case Exercise exercise -> concat(exercise.statement(), exercise.hints(), exercise.solution().stream().toList());
            case Qcm qcm -> concat(qcm.statement(), qcm.hints(), qcm.solution().stream().toList());
            case Prose _, Video _, PdfRef _, ExerciseRef _ -> List.of();
        };
    }

    private static List<Block> concat(List<Block> statement, List<Hint> hints, List<Solution> solution) {
        return Stream.of(
                        statement.stream(),
                        hints.stream().flatMap(hint -> hint.content().stream()),
                        solution.stream().flatMap(found -> found.content().stream()))
                .flatMap(stream -> stream)
                .toList();
    }
}
