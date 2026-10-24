package io.github.lucasssoh.boussole.core.learn.reward;

import io.github.lucasssoh.boussole.format.model.Domain;

/** A field of knowledge the learner has points in, and their specialty rank there. */
public record FieldStanding(Domain domain, double points, Rank rank) {
}
