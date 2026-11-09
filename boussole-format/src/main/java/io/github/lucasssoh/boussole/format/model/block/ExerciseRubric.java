package io.github.lucasssoh.boussole.format.model.block;

import io.github.lucasssoh.boussole.format.diagnostic.SourcePosition;
import java.util.List;

/**
 * The marking scheme of a free exercise: what an exam corrector would check, each criterion with
 * its weight. The learner ticks those their answer meets, once they have read the solution.
 */
public record ExerciseRubric(List<RubricCriterion> criteria, SourcePosition position) {

    public ExerciseRubric {
        criteria = List.copyOf(criteria);
    }

    /** The weight of every criterion together. */
    public double total() {
        return criteria.stream().mapToDouble(RubricCriterion::weight).sum();
    }
}
