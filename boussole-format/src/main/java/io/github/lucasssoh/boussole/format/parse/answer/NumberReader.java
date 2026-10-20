package io.github.lucasssoh.boussole.format.parse.answer;

import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCode;
import io.github.lucasssoh.boussole.format.model.expectation.Rational;
import io.github.lucasssoh.boussole.format.parse.DirectiveAttributes;
import io.github.lucasssoh.boussole.format.parse.InvalidAttributeException;
import io.github.lucasssoh.boussole.format.parse.ParseContext;
import java.util.Optional;

/** Reads the numbers of answers, reporting those that are not. */
final class NumberReader {

    private NumberReader() {
    }

    static Optional<Rational> read(String text, int line, ParseContext context) {
        Optional<Rational> number = Rational.parse(text);
        if (number.isEmpty()) {
            context.report(DiagnosticCode.F024, line, "`" + text + "` is not a number (write 2.5 or 3/4)");
        }
        return number;
    }

    /** A tolerance, 0 or more; {@code where} names it in the message. */
    static Optional<Rational> tolerance(String text, int line, ParseContext context, String where) {
        Optional<Rational> tolerance = Rational.parse(text).filter(number -> number.compareTo(Rational.ZERO) >= 0);
        if (tolerance.isEmpty()) {
            context.report(DiagnosticCode.F024, line, where + " `" + text + "` is not a number, 0 or more");
        }
        return tolerance;
    }

    /** The {@code tolerance} attribute of an exercise, 0 when absent. */
    static Rational toleranceAttribute(DirectiveAttributes attributes) {
        return attributes.text("tolerance")
                .map(value -> Rational.parse(value)
                        .filter(number -> number.compareTo(Rational.ZERO) >= 0)
                        .orElseThrow(() -> new InvalidAttributeException("tolerance", "must be a number, 0 or more, not `" + value + "`")))
                .orElse(Rational.ZERO);
    }
}
