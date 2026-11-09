package io.github.lucasssoh.boussole.format.model.block;

/** One point a corrector checks in a free exercise ({@code text}, inline Markdown), and what it weighs. */
public record RubricCriterion(String text, double weight) {
}
