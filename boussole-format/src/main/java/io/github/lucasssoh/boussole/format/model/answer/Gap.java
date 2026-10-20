package io.github.lucasssoh.boussole.format.model.answer;

import java.util.List;

/** A gap {@code [[number]]} of a fill exercise and what it accepts. */
public record Gap(int number, List<AcceptedText> accepted) {
}
