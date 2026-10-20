package io.github.lucasssoh.boussole.format.model.answer;

import java.util.List;

/** Accepted texts; unless {@code strict}, case is ignored. */
public record TextAnswer(List<AcceptedText> accepted, boolean strict) implements Answer {
}
