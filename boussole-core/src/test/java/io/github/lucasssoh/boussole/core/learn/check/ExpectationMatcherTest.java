package io.github.lucasssoh.boussole.core.learn.check;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.lucasssoh.boussole.format.model.expectation.AcceptedText;
import io.github.lucasssoh.boussole.format.model.expectation.ChoiceExpectation;
import io.github.lucasssoh.boussole.format.model.expectation.NumberExpectation;
import io.github.lucasssoh.boussole.format.model.expectation.Rational;
import io.github.lucasssoh.boussole.format.model.expectation.TextExpectation;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ExpectationMatcherTest {

    @Nested
    class Text {

        private final TextExpectation forwarding = new TextExpectation(
                List.of(new AcceptedText.Exact("le forwarding"), new AcceptedText.Exact("FIB")), false);

        @Test
        void ignores_case_and_extra_spaces() {
            assertThat(ExpectationMatcher.matches(forwarding, "  Le   Forwarding ")).isTrue();
            assertThat(ExpectationMatcher.matches(forwarding, "fib")).isTrue();
            assertThat(ExpectationMatcher.matches(forwarding, "routage")).isFalse();
        }

        @Test
        void keeps_case_when_strict() {
            TextExpectation strict = new TextExpectation(List.of(new AcceptedText.Exact("FIB")), true);

            assertThat(ExpectationMatcher.matches(strict, "FIB")).isTrue();
            assertThat(ExpectationMatcher.matches(strict, "fib")).isFalse();
        }

        @Test
        void accepts_what_a_regular_expression_matches() {
            TextExpectation regex = new TextExpectation(List.of(new AcceptedText.Matching(
                    Pattern.compile("(le )?forwarding", Pattern.CASE_INSENSITIVE))), false);

            assertThat(ExpectationMatcher.matches(regex, "Forwarding")).isTrue();
            assertThat(ExpectationMatcher.matches(regex, "le forwardings")).isFalse();
        }
    }

    @Nested
    class Number {

        private static Rational number(String text) {
            return Rational.parse(text).orElseThrow();
        }

        @Test
        void accepts_a_fraction_a_decimal_or_a_comma() {
            NumberExpectation threeQuarters = new NumberExpectation(List.of(number("3/4")), Rational.ZERO, Optional.empty());

            assertThat(ExpectationMatcher.matches(threeQuarters, "0.75")).isTrue();
            assertThat(ExpectationMatcher.matches(threeQuarters, "0,75")).isTrue();
            assertThat(ExpectationMatcher.matches(threeQuarters, "6/8")).isTrue();
            assertThat(ExpectationMatcher.matches(threeQuarters, "0.7")).isFalse();
        }

        @Test
        void accepts_a_value_within_the_tolerance() {
            NumberExpectation third = new NumberExpectation(List.of(number("1/3")), number("0.01"), Optional.empty());

            assertThat(ExpectationMatcher.matches(third, "0.33")).isTrue();
            assertThat(ExpectationMatcher.matches(third, "0.32")).isFalse();
        }

        @Test
        void forgives_the_unit_typed_and_a_typographic_minus() {
            NumberExpectation bits = new NumberExpectation(List.of(number("-48")), Rational.ZERO, Optional.of("bits"));

            assertThat(ExpectationMatcher.matches(bits, "−48 bits")).isTrue();
        }

        @Test
        void refuses_what_is_not_a_number() {
            NumberExpectation zero = new NumberExpectation(List.of(Rational.ZERO), Rational.ZERO, Optional.empty());

            assertThat(ExpectationMatcher.matches(zero, "zero")).isFalse();
            assertThat(ExpectationMatcher.matches(zero, "")).isFalse();
        }
    }

    @Test
    void a_choice_is_right_when_the_right_option_is_picked() {
        ChoiceExpectation choice = new ChoiceExpectation(List.of("convexe", "concave"), 0);

        assertThat(ExpectationMatcher.matches(choice, "convexe")).isTrue();
        assertThat(ExpectationMatcher.matches(choice, "concave")).isFalse();
    }
}
