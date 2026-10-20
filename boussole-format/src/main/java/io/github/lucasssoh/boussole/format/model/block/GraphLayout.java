package io.github.lucasssoh.boussole.format.model.block;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/**
 * How Boussole places the nodes of a graph whose author did not: on a circle, on one row (a path,
 * a sequence), or in layers from the first node (a tree, a flow from left to right).
 */
public enum GraphLayout {
    CIRCLE,
    ROW,
    LAYERS;

    public String attributeValue() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static Optional<GraphLayout> fromAttribute(String value) {
        return Arrays.stream(values()).filter(layout -> layout.attributeValue().equals(value)).findFirst();
    }
}
