package io.github.lucasssoh.boussole.format.model.answer;

import io.github.lucasssoh.boussole.format.model.expectation.Expectation;

/** The answer of a numeric or text exercise: one value. */
public record SingleAnswer(Expectation expected) implements Answer {
}
