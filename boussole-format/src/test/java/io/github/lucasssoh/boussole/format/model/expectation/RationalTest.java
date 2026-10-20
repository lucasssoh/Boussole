package io.github.lucasssoh.boussole.format.model.expectation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RationalTest {

    @Test
    void decimalsAndFractionsAreExact() {
        assertThat(parse("0.75")).isEqualTo(parse("3/4")).isEqualTo(parse("0,75")).isEqualTo(parse("6/8"));
        assertThat(parse("-2.5")).hasToString("-5/2");
        assertThat(parse("48")).hasToString("48");
        assertThat(parse("1.5/3")).isEqualTo(parse("1/2"));
    }

    @Test
    void anythingElseIsNotANumber() {
        assertThat(Rational.parse("quarante")).isEmpty();
        assertThat(Rational.parse("1/0")).isEmpty();
        assertThat(Rational.parse("1e3")).isEmpty();
        assertThat(Rational.parse("")).isEmpty();
    }

    @Test
    void comparesAndMeasuresDistance() {
        assertThat(parse("1/3").compareTo(parse("0.33"))).isPositive();
        assertThat(parse("0.3").subtract(parse("1/3")).abs()).isEqualTo(parse("1/30"));
    }

    private static Rational parse(String text) {
        return Rational.parse(text).orElseThrow();
    }
}
