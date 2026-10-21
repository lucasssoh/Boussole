package io.github.lucasssoh.boussole.format.model;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/** What an author did to a session. */
public enum AuthorshipRole {
    WROTE,
    REVISED,
    TRANSLATED;

    public String attributeValue() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static Optional<AuthorshipRole> fromAttribute(String value) {
        return Arrays.stream(values()).filter(role -> role.attributeValue().equals(value)).findFirst();
    }
}
