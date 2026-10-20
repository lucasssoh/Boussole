package io.github.lucasssoh.boussole.format.parse;

import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;

/**
 * The attributes of a directive, read by {@link DirectiveAttributeParser}. The typed getters throw
 * {@link InvalidAttributeException} on a value they cannot read.
 */
public record DirectiveAttributes(Map<String, String> values) {

    public static DirectiveAttributes none() {
        return new DirectiveAttributes(Map.of());
    }

    public Set<String> names() {
        return values.keySet();
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    public Optional<String> text(String name) {
        return Optional.ofNullable(values.get(name));
    }

    /** For a required attribute, already checked to be present. */
    public String required(String name) {
        return text(name).orElseThrow(() -> new InvalidAttributeException(name, "is required"));
    }

    public OptionalInt positiveInteger(String name) {
        Optional<String> value = text(name);
        if (value.isEmpty()) {
            return OptionalInt.empty();
        }
        try {
            int number = Integer.parseInt(value.get());
            if (number < 1) {
                throw new InvalidAttributeException(name, "must be 1 or more, not " + number);
            }
            return OptionalInt.of(number);
        } catch (NumberFormatException e) {
            throw new InvalidAttributeException(name, "must be a whole number, not `" + value.get() + "`");
        }
    }

    public boolean flag(String name) {
        return switch (text(name).orElse("false")) {
            case "true" -> true;
            case "false" -> false;
            case String other -> throw new InvalidAttributeException(name, "must be true or false, not `" + other + "`");
        };
    }
}
