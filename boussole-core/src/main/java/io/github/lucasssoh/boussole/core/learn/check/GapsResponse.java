package io.github.lucasssoh.boussole.core.learn.check;

import java.util.Map;

/** What the learner typed or picked in each gap of a fill exercise, by gap number. */
public record GapsResponse(Map<Integer, String> values) implements LearnerResponse {

    public GapsResponse {
        values = Map.copyOf(values);
    }
}
