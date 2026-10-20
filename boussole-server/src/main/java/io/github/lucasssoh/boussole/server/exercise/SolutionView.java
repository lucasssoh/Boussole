package io.github.lucasssoh.boussole.server.exercise;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.github.lucasssoh.boussole.server.block.BlockView;
import io.github.lucasssoh.boussole.server.block.BlockViewMapper;
import io.github.lucasssoh.boussole.core.learn.RevealedSolution;
import io.github.lucasssoh.boussole.core.learn.check.ExpectationText;
import io.github.lucasssoh.boussole.format.model.answer.FillAnswer;
import io.github.lucasssoh.boussole.format.model.answer.FillGap;
import io.github.lucasssoh.boussole.format.model.answer.MatchAnswer;
import io.github.lucasssoh.boussole.format.model.answer.OrderAnswer;
import io.github.lucasssoh.boussole.format.model.answer.SingleAnswer;
import io.github.lucasssoh.boussole.format.model.block.ExerciseBlock;
import io.github.lucasssoh.boussole.format.model.block.McqBlock;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** The worked solution, and what was expected, now that the learner may see them. */
record SolutionView(List<BlockView> blocks, ExpectedView expected) {

    static SolutionView of(RevealedSolution revealed, BlockViewMapper mapper) {
        return new SolutionView(
                revealed.solution().map(solution -> mapper.blocks(solution.content())).orElse(List.of()),
                expected(revealed));
    }

    private static ExpectedView expected(RevealedSolution revealed) {
        return switch (revealed.question()) {
            case McqBlock mcq -> new ExpectedView.Choices(mcq.choices().stream()
                    .map(choice -> new ExpectedView.Choice(choice.correct(), choice.feedback().orElse(null)))
                    .toList());
            case ExerciseBlock exercise when exercise.answer().isPresent() -> switch (exercise.answer().get()) {
                case SingleAnswer single -> new ExpectedView.Value(ExpectationText.of(single.expected()));
                case FillAnswer fill -> new ExpectedView.Gaps(fill.gaps().stream()
                        .collect(Collectors.toMap(FillGap::number, gap -> ExpectationText.of(gap.expected()))));
                case OrderAnswer order -> new ExpectedView.Order(order.items());
                case MatchAnswer match -> new ExpectedView.Pairs(match.pairs().stream()
                        .map(pair -> List.of(pair.left(), pair.right()))
                        .toList());
            };
            default -> new ExpectedView.Nothing();
        };
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "kind")
    @JsonSubTypes({
        @JsonSubTypes.Type(value = ExpectedView.Choices.class, name = "choices"),
        @JsonSubTypes.Type(value = ExpectedView.Value.class, name = "value"),
        @JsonSubTypes.Type(value = ExpectedView.Gaps.class, name = "gaps"),
        @JsonSubTypes.Type(value = ExpectedView.Order.class, name = "order"),
        @JsonSubTypes.Type(value = ExpectedView.Pairs.class, name = "pairs"),
        @JsonSubTypes.Type(value = ExpectedView.Nothing.class, name = "nothing")
    })
    sealed interface ExpectedView {

        /** Every choice of the MCQ, in file order, right or not, with its feedback. */
        record Choices(List<Choice> choices) implements ExpectedView {
        }

        record Choice(boolean correct, String feedback) {
        }

        record Value(String value) implements ExpectedView {
        }

        record Gaps(Map<Integer, String> values) implements ExpectedView {
        }

        record Order(List<String> items) implements ExpectedView {
        }

        /** Each pair as {@code [left, right]}. */
        record Pairs(List<List<String>> pairs) implements ExpectedView {
        }

        /** A free exercise: the solution says it all. */
        record Nothing() implements ExpectedView {
        }
    }
}
