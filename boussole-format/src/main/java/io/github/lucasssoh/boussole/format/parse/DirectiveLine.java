package io.github.lucasssoh.boussole.format.parse;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The opening line of a directive, {@code :::name[label]{attributes}} or
 * {@code ::name[label]{attributes}}, with its attributes still unread.
 */
public record DirectiveLine(String name, String label, String attributes, int colons) {

    private static final String BODY = "([a-z][a-z-]*)(?:\\[([^\\]]*)\\])?(?:\\{([^}]*)\\})?\\s*";
    private static final Pattern CONTAINER = Pattern.compile("(:{3,})" + BODY);
    private static final Pattern LEAF = Pattern.compile("(::)" + BODY);
    private static final Pattern CLOSING = Pattern.compile("(:{3,})\\s*");

    public static Optional<DirectiveLine> container(String line) {
        return match(CONTAINER, line);
    }

    public static Optional<DirectiveLine> leaf(String line) {
        return match(LEAF, line);
    }

    /** The number of colons of a closing line {@code :::}, if {@code line} is one. */
    public static Optional<Integer> closing(String line) {
        Matcher matcher = CLOSING.matcher(line);
        return matcher.matches() ? Optional.of(matcher.group(1).length()) : Optional.empty();
    }

    private static Optional<DirectiveLine> match(Pattern pattern, String line) {
        Matcher matcher = pattern.matcher(line);
        if (!matcher.matches()) {
            return Optional.empty();
        }
        return Optional.of(new DirectiveLine(
                matcher.group(2),
                Optional.ofNullable(matcher.group(3)).orElse(""),
                Optional.ofNullable(matcher.group(4)).orElse(""),
                matcher.group(1).length()));
    }
}
