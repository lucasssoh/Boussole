package io.github.lucasssoh.boussole.core.learn.reward;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.lucasssoh.boussole.core.learn.check.CheckResult;
import io.github.lucasssoh.boussole.core.learn.check.CheckedItem;
import io.github.lucasssoh.boussole.core.learn.check.ChoicesResponse;
import io.github.lucasssoh.boussole.core.learn.check.GapsResponse;
import io.github.lucasssoh.boussole.core.learn.check.MatchResponse;
import io.github.lucasssoh.boussole.core.learn.check.SelfGrade;
import io.github.lucasssoh.boussole.core.learn.check.SelfGradeResponse;
import io.github.lucasssoh.boussole.core.learn.check.Verdict;
import io.github.lucasssoh.boussole.format.diagnostic.SourcePosition;
import io.github.lucasssoh.boussole.format.model.answer.FillAnswer;
import io.github.lucasssoh.boussole.format.model.answer.FillGap;
import io.github.lucasssoh.boussole.format.model.answer.MatchAnswer;
import io.github.lucasssoh.boussole.format.model.answer.MatchPair;
import io.github.lucasssoh.boussole.format.model.expectation.TextExpectation;
import io.github.lucasssoh.boussole.format.model.block.ExerciseBlock;
import io.github.lucasssoh.boussole.format.model.block.ExerciseType;
import io.github.lucasssoh.boussole.format.model.block.McqBlock;
import io.github.lucasssoh.boussole.format.model.block.McqChoice;
import io.github.lucasssoh.boussole.format.model.block.ProseBlock;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import org.junit.jupiter.api.Test;

class QuestionScoreTest {

    private static final SourcePosition HERE = new SourcePosition("s.md", 1);

    private static ExerciseBlock fill(String statement, int gaps, boolean hook) {
        List<FillGap> answer = java.util.stream.IntStream.rangeClosed(1, gaps)
                .mapToObj(number -> new FillGap(number, new TextExpectation(List.of(), false)))
                .toList();
        return new ExerciseBlock("e", ExerciseType.FILL, List.of(new ProseBlock(statement, List.of(), HERE)),
                Optional.of(new FillAnswer(answer)), List.of(), Optional.empty(), OptionalInt.empty(), hook, HERE);
    }

    private static CheckResult gaps(boolean... right) {
        List<CheckedItem> items = new java.util.ArrayList<>();
        for (int index = 0; index < right.length; index++) {
            items.add(new CheckedItem(String.valueOf(index + 1), right[index], Optional.empty()));
        }
        return new CheckResult(Verdict.PARTLY, items);
    }

    @Test
    void a_fill_earns_one_point_per_sentence_whose_gaps_are_all_right() {
        ExerciseBlock exercise = fill("Une tâche commence à [[1]] et les machines sont [[2]]. Donc $S_k \\le L/m$ et LS ≤ [[3]] + p.", 3, false);

        assertThat(QuestionScore.of(exercise, new GapsResponse(Map.of()), gaps(true, true, false))).isEqualTo(new QuestionScore(1, 2));
        assertThat(QuestionScore.possible(exercise)).isEqualTo(2);
    }

    @Test
    void a_formula_a_decimal_or_inline_code_never_ends_a_sentence() {
        ExerciseBlock exercise = fill("Avec $x = 3.5$, `a.b` vaut [[1]] et 2.5 donne [[2]].", 2, false);

        assertThat(QuestionScore.possible(exercise)).isEqualTo(1);
    }

    @Test
    void each_item_of_a_list_and_a_code_block_is_a_sentence() {
        ExerciseBlock exercise = fill("- couche [[1]]\n- couche [[2]]\n\n```\nip [[3]] show\nip [[4]] add\n```", 4, false);

        assertThat(QuestionScore.possible(exercise)).isEqualTo(3);
    }

    @Test
    void a_hook_earns_nothing() {
        ExerciseBlock exercise = fill("Devine : [[1]].", 1, true);

        assertThat(QuestionScore.of(exercise, new GapsResponse(Map.of()), gaps(true))).isEqualTo(QuestionScore.NOTHING);
        assertThat(QuestionScore.possible(exercise)).isZero();
    }

    @Test
    void a_free_exercise_earns_no_points_even_when_the_learner_grades_it_as_successful() {
        ExerciseBlock exercise = new ExerciseBlock("free", ExerciseType.FREE, List.of(),
                Optional.empty(), List.of(), Optional.empty(), OptionalInt.empty(), false, HERE);

        assertThat(QuestionScore.of(exercise, new SelfGradeResponse(SelfGrade.GOT_IT),
                new CheckResult(Verdict.CORRECT, List.of()))).isEqualTo(QuestionScore.NOTHING);
        assertThat(QuestionScore.possible(exercise)).isZero();
    }

    @Test
    void each_box_of_a_multiple_choice_mcq_earns_or_costs_its_share() {
        McqBlock mcq = new McqBlock("m", true, List.of(), List.of(
                new McqChoice("a", true, Optional.empty(), HERE), new McqChoice("b", true, Optional.empty(), HERE),
                new McqChoice("c", true, Optional.empty(), HERE), new McqChoice("d", false, Optional.empty(), HERE)),
                List.of(), Optional.empty(), OptionalInt.empty(), false, HERE);

        assertThat(QuestionScore.of(mcq, new ChoicesResponse(Set.of(0, 2, 3)), new CheckResult(Verdict.PARTLY, List.of())).earned())
                .isCloseTo(1.0 / 3, org.assertj.core.data.Offset.offset(1e-9));
        assertThat(QuestionScore.of(mcq, new ChoicesResponse(Set.of(3)), new CheckResult(Verdict.WRONG, List.of())).earned()).isZero();
    }

    @Test
    void a_pair_left_out_of_a_match_counts_nothing() {
        ExerciseBlock match = new ExerciseBlock("p", ExerciseType.MATCH, List.of(), Optional.of(new MatchAnswer(List.of(
                new MatchPair("a", "1"), new MatchPair("b", "2"), new MatchPair("c", "3"), new MatchPair("d", "4")))),
                List.of(), Optional.empty(), OptionalInt.empty(), false, HERE);
        CheckResult result = new CheckResult(Verdict.PARTLY, List.of(
                new CheckedItem("a", true, Optional.empty()), new CheckedItem("b", true, Optional.empty()),
                new CheckedItem("c", false, Optional.empty()), new CheckedItem("d", false, Optional.empty())));

        assertThat(QuestionScore.of(match, new MatchResponse(Map.of("a", "1", "b", "2", "c", "4")), result).earned()).isEqualTo(0.25);
    }
}
