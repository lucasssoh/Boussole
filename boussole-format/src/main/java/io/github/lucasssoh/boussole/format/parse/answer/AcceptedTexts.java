package io.github.lucasssoh.boussole.format.parse.answer;

import io.github.lucasssoh.boussole.format.diagnostic.Code;
import io.github.lucasssoh.boussole.format.model.expectation.AcceptedText;
import io.github.lucasssoh.boussole.format.parse.ParseContext;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/** Reads one accepted text: literal, or a regular expression after {@code re:}. */
final class AcceptedTexts {

    private static final String REGEX_PREFIX = "re:";

    private AcceptedTexts() {
    }

    static Optional<AcceptedText> read(String text, boolean strict, int line, ParseContext context) {
        if (!text.startsWith(REGEX_PREFIX)) {
            return Optional.of(new AcceptedText.Exact(text));
        }
        String regex = text.substring(REGEX_PREFIX.length()).strip();
        try {
            int flags = strict ? 0 : Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
            return Optional.of(new AcceptedText.Matching(Pattern.compile(regex, flags)));
        } catch (PatternSyntaxException e) {
            context.report(Code.F025, line, "invalid regular expression `" + regex + "`: " + e.getDescription());
            return Optional.empty();
        }
    }
}
