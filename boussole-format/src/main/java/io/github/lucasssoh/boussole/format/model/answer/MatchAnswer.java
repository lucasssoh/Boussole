package io.github.lucasssoh.boussole.format.model.answer;

import java.util.List;

/** The right pairs; the interface shows the right-hand sides shuffled. */
public record MatchAnswer(List<MatchPair> pairs) implements Answer {
}
