package io.github.lucasssoh.boussole.format.model.answer;

import java.util.List;
import java.util.Optional;

/** Accepted values, an absolute tolerance, and a unit displayed beside the input. */
public record NumericAnswer(List<Rational> values, Rational tolerance, Optional<String> unit) implements Answer {
}
