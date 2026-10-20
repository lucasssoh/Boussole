package io.github.lucasssoh.boussole.format.model.expectation;

/**
 * One value the learner must give: typed text, a number, or an option to choose. A numeric or
 * text exercise expects one; a fill exercise expects one per gap.
 */
public sealed interface Expectation permits TextExpectation, NumberExpectation, ChoiceExpectation {

    /** Whether the learner picks the value from a list rather than typing it. */
    boolean isChosen();
}
