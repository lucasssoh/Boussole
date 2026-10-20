package io.github.lucasssoh.boussole.format.model.answer;

import java.util.List;

public record FillAnswer(List<Gap> gaps, boolean strict) implements Answer {
}
