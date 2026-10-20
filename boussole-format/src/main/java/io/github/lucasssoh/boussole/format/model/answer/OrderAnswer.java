package io.github.lucasssoh.boussole.format.model.answer;

import java.util.List;

/** The items in the right order; the interface shows them shuffled. */
public record OrderAnswer(List<String> items) implements Answer {
}
