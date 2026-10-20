package io.github.lucasssoh.boussole.format.model.block;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/** The {@code type} attribute of an exercise. */
public enum ExerciseType {
    NUMERIC,
    TEXT,
    FILL,
    ORDER,
    MATCH,
    FREE;

    public String attributeValue() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static Optional<ExerciseType> fromAttribute(String value) {
        return Arrays.stream(values()).filter(type -> type.attributeValue().equals(value)).findFirst();
    }
}
