package io.github.lucasssoh.boussole.core.learn.check;

import io.github.lucasssoh.boussole.format.model.expectation.AcceptedText;
import io.github.lucasssoh.boussole.format.model.expectation.ChoiceExpectation;
import io.github.lucasssoh.boussole.format.model.expectation.Expectation;
import io.github.lucasssoh.boussole.format.model.expectation.NumberExpectation;
import io.github.lucasssoh.boussole.format.model.expectation.Rational;
import io.github.lucasssoh.boussole.format.model.expectation.TextExpectation;
import java.util.Locale;
import java.util.Optional;

/** Whether one value the learner gave is what an {@link Expectation} accepts, as {@code FORMAT.md} §5.6 says. */
public final class ExpectationMatcher {

    private ExpectationMatcher() {
    }

    public static boolean matches(Expectation expected, String given) {
        return switch (expected) {
            case TextExpectation text -> matchesText(text, collapse(given));
            case NumberExpectation number -> matchesNumber(number, given);
            case ChoiceExpectation choice -> choice.correctOption().equals(given);
        };
    }

    private static boolean matchesText(TextExpectation expected, String given) {
        return expected.accepted().stream().anyMatch(accepted -> switch (accepted) {
            case AcceptedText.Exact exact -> expected.strict()
                    ? collapse(exact.text()).equals(given)
                    : lower(collapse(exact.text())).equals(lower(given));
            case AcceptedText.Matching matching -> matching.pattern().matcher(given).matches();
        });
    }

    private static boolean matchesNumber(NumberExpectation expected, String given) {
        Optional<Rational> value = Rational.parse(withoutUnit(given.replace('−', '-'), expected.unit()));
        return value.isPresent() && expected.values().stream()
                .anyMatch(accepted -> value.get().subtract(accepted).abs().compareTo(expected.tolerance()) <= 0);
    }

    /** The unit is displayed beside the field; a learner who types it anyway is not wrong. */
    private static String withoutUnit(String given, Optional<String> unit) {
        String trimmed = given.strip();
        return unit.filter(trimmed::endsWith)
                .map(found -> trimmed.substring(0, trimmed.length() - found.length()))
                .orElse(trimmed);
    }

    private static String collapse(String text) {
        return text.strip().replaceAll("\\s+", " ");
    }

    private static String lower(String text) {
        return text.toLowerCase(Locale.ROOT);
    }
}
