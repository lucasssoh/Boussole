package io.github.lucasssoh.boussole.core.learn.check;

import java.util.Set;

/** The choices of an MCQ the learner ticked, by their index in the file, from 0. */
public record ChoicesResponse(Set<Integer> picked) implements LearnerResponse {

    public ChoicesResponse {
        picked = Set.copyOf(picked);
    }
}
