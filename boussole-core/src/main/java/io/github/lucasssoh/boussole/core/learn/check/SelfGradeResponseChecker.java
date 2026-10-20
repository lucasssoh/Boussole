package io.github.lucasssoh.boussole.core.learn.check;

import io.github.lucasssoh.boussole.format.model.block.ExerciseBlock;
import java.util.List;

/** A free exercise: the learner's own grade is the verdict. */
final class SelfGradeResponseChecker implements ResponseChecker<ExerciseBlock, SelfGradeResponse> {

    @Override
    public Class<SelfGradeResponse> responseType() {
        return SelfGradeResponse.class;
    }

    @Override
    public CheckResult check(ExerciseBlock exercise, SelfGradeResponse response) {
        Verdict verdict = switch (response.grade()) {
            case GOT_IT -> Verdict.CORRECT;
            case PARTLY -> Verdict.PARTLY;
            case NOT_YET -> Verdict.WRONG;
        };
        return new CheckResult(verdict, List.of());
    }
}
