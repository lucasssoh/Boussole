package io.github.lucasssoh.boussole.format.model.expectation;

import java.util.List;
import java.util.Optional;

/** A number, one of the accepted values within an absolute tolerance; the unit is displayed, not typed. */
public record NumberExpectation(List<Rational> values, Rational tolerance, Optional<String> unit) implements Expectation {

    @Override
    public boolean isChosen() {
        return false;
    }
}
