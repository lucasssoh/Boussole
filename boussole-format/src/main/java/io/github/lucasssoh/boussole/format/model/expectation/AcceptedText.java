package io.github.lucasssoh.boussole.format.model.expectation;

import java.util.regex.Pattern;

/** One accepted text answer: a literal, or a regular expression when the line starts with {@code re:}. */
public sealed interface AcceptedText {

    record Exact(String text) implements AcceptedText {
    }

    record Matching(Pattern pattern) implements AcceptedText {
    }
}
