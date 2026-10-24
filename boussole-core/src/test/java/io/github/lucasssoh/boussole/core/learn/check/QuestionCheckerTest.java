package io.github.lucasssoh.boussole.core.learn.check;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.lucasssoh.boussole.format.diagnostic.SourcePosition;
import io.github.lucasssoh.boussole.format.model.answer.Answer;
import io.github.lucasssoh.boussole.format.model.answer.FillAnswer;
import io.github.lucasssoh.boussole.format.model.answer.FillGap;
import io.github.lucasssoh.boussole.format.model.answer.MatchAnswer;
import io.github.lucasssoh.boussole.format.model.answer.MatchPair;
import io.github.lucasssoh.boussole.format.model.answer.OrderAnswer;
import io.github.lucasssoh.boussole.format.model.answer.SingleAnswer;
import io.github.lucasssoh.boussole.format.model.block.ExerciseBlock;
import io.github.lucasssoh.boussole.format.model.block.ExerciseType;
import io.github.lucasssoh.boussole.format.model.block.McqBlock;
import io.github.lucasssoh.boussole.format.model.block.McqChoice;
import io.github.lucasssoh.boussole.format.model.expectation.ChoiceExpectation;
import io.github.lucasssoh.boussole.format.model.expectation.NumberExpectation;
import io.github.lucasssoh.boussole.format.model.expectation.Rational;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class QuestionCheckerTest {

    private static final SourcePosition HERE = new SourcePosition("s.md", 1);

    private final QuestionChecker checker = new QuestionChecker();

    private static ExerciseBlock exercise(ExerciseType type, Answer answer) {
        return new ExerciseBlock("s11-a1", type, List.of(), Optional.ofNullable(answer), List.of(), Optional.empty(),
                OptionalInt.empty(), false, HERE);
    }

    private static McqChoice choice(String text, boolean correct, String feedback) {
        return new McqChoice(text, correct, Optional.ofNullable(feedback), HERE);
    }

    @Nested
    class Mcq {

        private final McqBlock datagram = new McqBlock("s11-a2", false, List.of(), List.of(
                choice("Datagramme", false, "Non : aucun état."),
                choice("Circuit virtuel", true, null),
                choice("Les deux", false, null)), List.of(), Optional.empty(), OptionalInt.empty(), false, HERE);

        @Test
        void the_right_choice_is_correct() {
            assertThat(checker.check(datagram, new ChoicesResponse(Set.of(1))).verdict()).isEqualTo(Verdict.CORRECT);
        }

        @Test
        void a_wrong_choice_brings_its_feedback() {
            CheckResult result = checker.check(datagram, new ChoicesResponse(Set.of(0)));

            assertThat(result.verdict()).isEqualTo(Verdict.WRONG);
            assertThat(result.items()).containsExactly(new CheckedItem("0", false, Optional.of("Non : aucun état.")));
        }

        @Test
        void some_right_choices_of_several_are_partly_right() {
            McqBlock several = new McqBlock("c1", true, List.of(), List.of(
                    choice("a", true, null), choice("b", true, null), choice("c", false, null)),
                    List.of(), Optional.empty(), OptionalInt.empty(), false, HERE);

            assertThat(checker.check(several, new ChoicesResponse(Set.of(0))).verdict()).isEqualTo(Verdict.PARTLY);
            assertThat(checker.check(several, new ChoicesResponse(Set.of(0, 1))).verdict()).isEqualTo(Verdict.CORRECT);
            assertThat(checker.check(several, new ChoicesResponse(Set.of(0, 1, 2))).verdict()).isEqualTo(Verdict.PARTLY);
        }

        @Test
        void refuses_a_choice_that_does_not_exist() {
            assertThatThrownBy(() -> checker.check(datagram, new ChoicesResponse(Set.of(3))))
                    .isInstanceOf(ResponseMismatchException.class);
        }
    }

    @Test
    void a_numeric_exercise_checks_the_value() {
        ExerciseBlock bits = exercise(ExerciseType.NUMERIC, new SingleAnswer(
                new NumberExpectation(List.of(Rational.parse("48").orElseThrow()), Rational.ZERO, Optional.of("bits"))));

        assertThat(checker.check(bits, new ValueResponse("48")).isCorrect()).isTrue();
        assertThat(checker.check(bits, new ValueResponse("6")).verdict()).isEqualTo(Verdict.WRONG);
    }

    @Test
    void a_fill_exercise_checks_each_gap() {
        ExerciseBlock fill = exercise(ExerciseType.FILL, new FillAnswer(List.of(
                new FillGap(1, new ChoiceExpectation(List.of("≤", "≥"), 0)),
                new FillGap(2, new NumberExpectation(List.of(Rational.parse("3").orElseThrow()), Rational.ZERO, Optional.empty())))));

        CheckResult result = checker.check(fill, new GapsResponse(Map.of(1, "≤", 2, "4")));

        assertThat(result.verdict()).isEqualTo(Verdict.PARTLY);
        assertThat(result.items()).extracting(CheckedItem::key, CheckedItem::correct)
                .containsExactly(org.assertj.core.groups.Tuple.tuple("1", true), org.assertj.core.groups.Tuple.tuple("2", false));
    }

    @Test
    void an_order_exercise_checks_each_position() {
        ExerciseBlock order = exercise(ExerciseType.ORDER, new OrderAnswer(List.of("a", "b", "c")));

        assertThat(checker.check(order, new OrderResponse(List.of("a", "b", "c"))).isCorrect()).isTrue();
        assertThat(checker.check(order, new OrderResponse(List.of("a", "c", "b"))).verdict()).isEqualTo(Verdict.PARTLY);
        assertThatThrownBy(() -> checker.check(order, new OrderResponse(List.of("a", "b"))))
                .isInstanceOf(ResponseMismatchException.class);
    }

    @Test
    void a_match_exercise_checks_each_pair() {
        ExerciseBlock match = exercise(ExerciseType.MATCH, new MatchAnswer(List.of(
                new MatchPair("max", "min"), new MatchPair("≤", "≥"))));

        assertThat(checker.check(match, new MatchResponse(Map.of("max", "min", "≤", "≥"))).isCorrect()).isTrue();
        assertThat(checker.check(match, new MatchResponse(Map.of("max", "≥", "≤", "min"))).verdict()).isEqualTo(Verdict.WRONG);
    }

    @Test
    void a_free_exercise_takes_the_learners_own_grade() {
        ExerciseBlock free = exercise(ExerciseType.FREE, null);

        assertThat(checker.check(free, new SelfGradeResponse(SelfGrade.PARTLY)).verdict()).isEqualTo(Verdict.PARTLY);
    }

    @Test
    void refuses_a_response_of_the_wrong_kind() {
        ExerciseBlock order = exercise(ExerciseType.ORDER, new OrderAnswer(List.of("a", "b")));

        assertThatThrownBy(() -> checker.check(order, new ValueResponse("a b")))
                .isInstanceOf(ResponseMismatchException.class)
                .hasMessageContaining("OrderResponse");
    }
}
