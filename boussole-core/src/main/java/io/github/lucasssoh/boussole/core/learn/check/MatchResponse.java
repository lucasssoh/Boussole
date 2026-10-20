package io.github.lucasssoh.boussole.core.learn.check;

import java.util.Map;

/** The right-hand side the learner chose for each left-hand side of a match exercise. */
public record MatchResponse(Map<String, String> pairs) implements LearnerResponse {

    public MatchResponse {
        pairs = Map.copyOf(pairs);
    }
}
