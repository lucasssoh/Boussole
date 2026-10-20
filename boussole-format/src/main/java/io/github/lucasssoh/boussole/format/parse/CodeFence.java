package io.github.lucasssoh.boussole.format.parse;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tracks fenced code blocks, so that nothing inside them is read as a directive or a heading.
 * Feed it every line in order.
 */
final class CodeFence {

    private static final Pattern FENCE = Pattern.compile(" {0,3}(`{3,}|~{3,}).*");

    private Optional<String> open = Optional.empty();

    /** Whether {@code line} is code: inside a fence, or one of its fence lines. */
    boolean isCode(String line) {
        Matcher matcher = FENCE.matcher(line);
        if (open.isEmpty()) {
            if (matcher.matches()) {
                open = Optional.of(matcher.group(1));
                return true;
            }
            return false;
        }
        String fence = open.get();
        if (matcher.matches()
                && matcher.group(1).charAt(0) == fence.charAt(0)
                && matcher.group(1).length() >= fence.length()
                && line.strip().length() == matcher.group(1).length()) {
            open = Optional.empty();
        }
        return true;
    }
}
