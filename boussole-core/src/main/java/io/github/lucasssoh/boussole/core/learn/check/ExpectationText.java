package io.github.lucasssoh.boussole.core.learn.check;

import io.github.lucasssoh.boussole.format.model.expectation.AcceptedText;
import io.github.lucasssoh.boussole.format.model.expectation.ChoiceExpectation;
import io.github.lucasssoh.boussole.format.model.expectation.Expectation;
import io.github.lucasssoh.boussole.format.model.expectation.NumberExpectation;
import io.github.lucasssoh.boussole.format.model.expectation.Rational;
import io.github.lucasssoh.boussole.format.model.expectation.TextExpectation;

/** The expected value written for the learner, shown with the solution. */
public final class ExpectationText {

    private ExpectationText() {
    }

    public static String of(Expectation expected) {
        return switch (expected) {
            case TextExpectation text -> text.accepted().stream()
                    .filter(AcceptedText.Exact.class::isInstance)
                    .map(accepted -> ((AcceptedText.Exact) accepted).text())
                    .findFirst()
                    .orElseGet(() -> text.accepted().getFirst() instanceof AcceptedText.Matching matching
                            ? matching.pattern().pattern()
                            : "");
            case NumberExpectation number -> number.values().getFirst()
                    + (number.tolerance().equals(Rational.ZERO) ? "" : " ± " + number.tolerance())
                    + number.unit().map(unit -> " " + unit).orElse("");
            case ChoiceExpectation choice -> choice.correctOption();
        };
    }
}
