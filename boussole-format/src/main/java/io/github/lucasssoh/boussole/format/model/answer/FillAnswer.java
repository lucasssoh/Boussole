package io.github.lucasssoh.boussole.format.model.answer;

import java.util.List;

/** The gaps of a fill exercise, in the order of the answer. */
public record FillAnswer(List<FillGap> gaps) implements Answer {
}
