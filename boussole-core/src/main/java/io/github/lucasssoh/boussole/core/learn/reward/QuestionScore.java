package io.github.lucasssoh.boussole.core.learn.reward;

import io.github.lucasssoh.boussole.core.learn.check.CheckResult;
import io.github.lucasssoh.boussole.core.learn.check.CheckedItem;
import io.github.lucasssoh.boussole.core.learn.check.ChoicesResponse;
import io.github.lucasssoh.boussole.core.learn.check.LearnerResponse;
import io.github.lucasssoh.boussole.core.learn.check.MatchResponse;
import io.github.lucasssoh.boussole.format.model.answer.FillAnswer;
import io.github.lucasssoh.boussole.format.model.answer.MatchAnswer;
import io.github.lucasssoh.boussole.format.model.block.Block;
import io.github.lucasssoh.boussole.format.model.block.ExerciseBlock;
import io.github.lucasssoh.boussole.format.model.block.McqBlock;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * What an answer is worth, before the weight of the occasion and the level of the track: what it
 * {@code earned} out of what it was {@code possible} to earn.
 *
 * <ul>
 *   <li>A value, a single-choice MCQ, an order, a free exercise graded right: 1, all or nothing.</li>
 *   <li>A fill: 1 per sentence whose gaps are all right ({@link GapSentences}).</li>
 *   <li>A multiple-choice MCQ, a match: 1 split between its elements; each right one earns its
 *       share, each wrong one takes it away, one left out counts nothing; never below 0.</li>
 *   <li>A hook: 0.</li>
 * </ul>
 */
public record QuestionScore(double earned, double possible) {

    public static final QuestionScore NOTHING = new QuestionScore(0, 0);

    public static QuestionScore of(Block question, LearnerResponse response, CheckResult result) {
        return switch (question) {
            case McqBlock mcq when mcq.hook() -> NOTHING;
            case ExerciseBlock exercise when exercise.hook() -> NOTHING;
            case McqBlock mcq when mcq.multiple() -> multipleChoice(mcq, (ChoicesResponse) response);
            case ExerciseBlock exercise when exercise.answer().orElse(null) instanceof FillAnswer fill -> fill(exercise, fill, result);
            case ExerciseBlock exercise when exercise.answer().orElse(null) instanceof MatchAnswer match ->
                    shares(result, match.pairs().size(), ((MatchResponse) response).pairs().keySet());
            default -> new QuestionScore(result.isCorrect() ? 1 : 0, 1);
        };
    }

    /** What a question may earn at most, whatever the answer. */
    public static double possible(Block question) {
        return switch (question) {
            case McqBlock mcq when mcq.hook() -> 0;
            case ExerciseBlock exercise when exercise.hook() -> 0;
            case ExerciseBlock exercise when exercise.answer().orElse(null) instanceof FillAnswer fill ->
                    sentences(exercise, fill).values().stream().distinct().count();
            case McqBlock _, ExerciseBlock _ -> 1;
            default -> 0;
        };
    }

    private static QuestionScore multipleChoice(McqBlock mcq, ChoicesResponse response) {
        long right = mcq.choices().stream().filter(choice -> choice.correct()).count();
        double share = right == 0 ? 0 : 1.0 / right;
        long rightPicked = response.picked().stream().filter(index -> mcq.choices().get(index).correct()).count();
        long wrongPicked = response.picked().size() - rightPicked;
        return new QuestionScore(Math.max(0, share * (rightPicked - wrongPicked)), 1);
    }

    private static QuestionScore shares(CheckResult result, int elements, Set<String> answered) {
        double share = elements == 0 ? 0 : 1.0 / elements;
        long right = result.items().stream().filter(CheckedItem::correct).count();
        long wrong = result.items().stream().filter(item -> !item.correct() && answered.contains(item.key())).count();
        return new QuestionScore(Math.max(0, share * (right - wrong)), 1);
    }

    private static QuestionScore fill(ExerciseBlock exercise, FillAnswer fill, CheckResult result) {
        Map<Integer, Integer> sentenceOf = sentences(exercise, fill);
        Set<String> wrong = result.items().stream().filter(item -> !item.correct()).map(CheckedItem::key).collect(Collectors.toSet());
        Set<Integer> spoilt = sentenceOf.entrySet().stream()
                .filter(entry -> wrong.contains(String.valueOf(entry.getKey())))
                .map(Map.Entry::getValue)
                .collect(Collectors.toSet());
        long all = sentenceOf.values().stream().distinct().count();
        return new QuestionScore(all - spoilt.size(), all);
    }

    /** Every gap of the answer in a sentence; a gap the statement does not show counts as its own. */
    private static Map<Integer, Integer> sentences(ExerciseBlock exercise, FillAnswer fill) {
        Map<Integer, Integer> found = GapSentences.of(exercise.statement());
        return fill.gaps().stream().collect(Collectors.toMap(gap -> gap.number(),
                gap -> found.getOrDefault(gap.number(), -1 - gap.number())));
    }
}
