package io.github.lucasssoh.boussole.format.validate.rule;

import io.github.lucasssoh.boussole.format.diagnostic.Code;
import io.github.lucasssoh.boussole.format.diagnostic.Diagnostics;
import io.github.lucasssoh.boussole.format.model.block.Exercise;
import io.github.lucasssoh.boussole.format.model.block.Qcm;
import io.github.lucasssoh.boussole.format.validate.ValidationContext;
import io.github.lucasssoh.boussole.format.validate.ValidationRule;

/** P001: each notion ends with something to do, an exercise or a qcm. */
public final class NotionExerciseRule implements ValidationRule {

    @Override
    public void check(ValidationContext context, Diagnostics diagnostics) {
        Notions.of(context.course())
                .filter(notion -> notion.blocks().stream().noneMatch(block -> block instanceof Exercise || block instanceof Qcm))
                .forEach(notion -> diagnostics.report(Code.P001, notion.position(),
                        "notion `" + notion.id() + "` has no exercise or qcm for the learner to try"));
    }
}
