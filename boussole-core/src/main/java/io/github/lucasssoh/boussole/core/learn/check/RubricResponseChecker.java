package io.github.lucasssoh.boussole.core.learn.check;

import io.github.lucasssoh.boussole.format.model.block.ExerciseRubric;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

/**
 * A free exercise with a rubric: each criterion ticked is met. Right when all are, partly when some
 * are; the share of the rubric's weight met gives the stars.
 */
final class RubricResponseChecker implements ResponseChecker<ExerciseRubric, RubricResponse> {

    @Override
    public Class<RubricResponse> responseType() {
        return RubricResponse.class;
    }

    @Override
    public CheckResult check(ExerciseRubric rubric, RubricResponse response) {
        int size = rubric.criteria().size();
        for (int index : response.met()) {
            if (index < 0 || index >= size) {
                throw new ResponseMismatchException("the rubric has no criterion " + index);
            }
        }
        List<CheckedItem> items = new ArrayList<>();
        double met = 0;
        for (int index = 0; index < size; index++) {
            boolean ticked = response.met().contains(index);
            items.add(CheckedItem.of(index, ticked));
            met += ticked ? rubric.criteria().get(index).weight() : 0;
        }
        double total = rubric.total();
        double share = total > 0 ? met / total : 0;
        return new CheckResult(Verdict.of(response.met().size(), size), items, OptionalDouble.of(share));
    }
}
