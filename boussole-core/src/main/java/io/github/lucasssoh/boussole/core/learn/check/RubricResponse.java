package io.github.lucasssoh.boussole.core.learn.check;

import java.util.Set;

/**
 * A free exercise with a rubric: the criteria the learner ticks, by their place in the rubric
 * (from 0), once they have compared their answer with the solution.
 */
public record RubricResponse(Set<Integer> met) implements LearnerResponse {

    public RubricResponse {
        met = met == null ? Set.of() : Set.copyOf(met);
    }
}
