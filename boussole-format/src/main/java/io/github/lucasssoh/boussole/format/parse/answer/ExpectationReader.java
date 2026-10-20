package io.github.lucasssoh.boussole.format.parse.answer;

import io.github.lucasssoh.boussole.format.diagnostic.Code;
import io.github.lucasssoh.boussole.format.model.expectation.AcceptedText;
import io.github.lucasssoh.boussole.format.model.expectation.ChoiceExpectation;
import io.github.lucasssoh.boussole.format.model.expectation.Expectation;
import io.github.lucasssoh.boussole.format.model.expectation.NumberExpectation;
import io.github.lucasssoh.boussole.format.model.expectation.Rational;
import io.github.lucasssoh.boussole.format.model.expectation.TextExpectation;
import io.github.lucasssoh.boussole.format.parse.ParseContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads what one gap of a fill exercise expects, from the text after {@code N:}:
 * <ul>
 *   <li>{@code choice *right | wrong | other}: an option to pick, the right one starred;</li>
 *   <li>{@code number 62} or {@code number 3/4 ±0.01}: a number;</li>
 *   <li>anything else: typed text, alternatives separated by {@code |} between spaces.</li>
 * </ul>
 */
final class ExpectationReader {

    static final Pattern ALTERNATIVES = Pattern.compile("\\s+\\|\\s+");
    private static final Pattern CHOICE = Pattern.compile("choice\\s+(.+)");
    private static final Pattern NUMBER = Pattern.compile("number\\s+(\\S+)(?:\\s*(?:±|\\+-)\\s*(\\S+))?");
    private static final String RIGHT_OPTION = "*";

    private ExpectationReader() {
    }

    static Optional<Expectation> read(String text, boolean strict, int line, ParseContext context) {
        Matcher choice = CHOICE.matcher(text);
        if (choice.matches()) {
            return choice(choice.group(1), line, context);
        }
        Matcher number = NUMBER.matcher(text);
        if (number.matches()) {
            return number(number.group(1), number.group(2), line, context);
        }
        return text(List.of(ALTERNATIVES.split(text.strip())), strict, line, context).map(Expectation.class::cast);
    }

    /** Typed text accepting each of {@code alternatives}. */
    static Optional<TextExpectation> text(List<String> alternatives, boolean strict, int line, ParseContext context) {
        List<AcceptedText> accepted = new ArrayList<>();
        for (String alternative : alternatives) {
            Optional<AcceptedText> text = AcceptedTexts.read(alternative, strict, line, context);
            if (text.isEmpty()) {
                return Optional.empty();
            }
            accepted.add(text.get());
        }
        return Optional.of(new TextExpectation(List.copyOf(accepted), strict));
    }

    private static Optional<Expectation> choice(String options, int line, ParseContext context) {
        List<String> written = List.of(ALTERNATIVES.split(options.strip()));
        List<String> plain = written.stream().map(option -> option.startsWith(RIGHT_OPTION) ? option.substring(1).strip() : option).toList();
        List<Integer> right = new ArrayList<>();
        for (int index = 0; index < written.size(); index++) {
            if (written.get(index).startsWith(RIGHT_OPTION)) {
                right.add(index);
            }
        }
        if (written.size() < 2 || right.size() != 1) {
            context.report(Code.F021, line, "a choice gap reads `choice *right | wrong | other`: two options or more, one starred");
            return Optional.empty();
        }
        return Optional.of(new ChoiceExpectation(plain, right.getFirst()));
    }

    private static Optional<Expectation> number(String value, String tolerance, int line, ParseContext context) {
        return Numbers.read(value, line, context).flatMap(number -> (tolerance == null
                        ? Optional.of(Rational.ZERO)
                        : Numbers.tolerance(tolerance, line, context, "tolerance"))
                .map(margin -> new NumberExpectation(List.of(number), margin, Optional.empty())));
    }
}
