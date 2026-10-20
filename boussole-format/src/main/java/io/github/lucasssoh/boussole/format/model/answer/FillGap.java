package io.github.lucasssoh.boussole.format.model.answer;

import io.github.lucasssoh.boussole.format.model.expectation.Expectation;

/** A gap {@code [[number]]} of a fill exercise, and what it expects: typed text, a number or a choice. */
public record FillGap(int number, Expectation expected) {
}
