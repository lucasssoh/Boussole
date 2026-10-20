package io.github.lucasssoh.boussole.format.parse;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Reads the inside of {@code {…}}: {@code key=value}, {@code key="value with spaces"},
 * {@code #id} for {@code id=id}, and a bare {@code key} for {@code key=true}.
 */
final class DirectiveAttributeParser {

    private final String source;
    private int index;

    private DirectiveAttributeParser(String source) {
        this.source = source;
    }

    /** @throws InvalidAttributeException when the text cannot be read, or an attribute is repeated */
    static DirectiveAttributes parse(String source) {
        return new DirectiveAttributeParser(source).readAll();
    }

    private DirectiveAttributes readAll() {
        Map<String, String> values = new LinkedHashMap<>();
        skipSpaces();
        while (index < source.length()) {
            String name;
            String value;
            if (source.charAt(index) == '#') {
                index++;
                name = "id";
                value = readWord();
            } else {
                name = readWord();
                if (index < source.length() && source.charAt(index) == '=') {
                    index++;
                    value = readValue(name);
                } else {
                    value = "true";
                }
            }
            if (name.isEmpty()) {
                throw new InvalidAttributeException(source, "cannot be read near `" + source.substring(index) + "`");
            }
            if (values.putIfAbsent(name, value) != null) {
                throw new InvalidAttributeException(name, "is given twice");
            }
            skipSpaces();
        }
        return new DirectiveAttributes(Map.copyOf(values));
    }

    private String readValue(String name) {
        if (index < source.length() && (source.charAt(index) == '"' || source.charAt(index) == '\'')) {
            char quote = source.charAt(index);
            int end = source.indexOf(quote, index + 1);
            if (end < 0) {
                throw new InvalidAttributeException(name, "has an unclosed quote");
            }
            String value = source.substring(index + 1, end);
            index = end + 1;
            return value;
        }
        return readWord();
    }

    private String readWord() {
        int start = index;
        while (index < source.length()
                && !Character.isWhitespace(source.charAt(index))
                && source.charAt(index) != '=') {
            index++;
        }
        return source.substring(start, index);
    }

    private void skipSpaces() {
        while (index < source.length() && Character.isWhitespace(source.charAt(index))) {
            index++;
        }
    }
}
