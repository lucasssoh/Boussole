package io.github.lucasssoh.boussole.format.validate.rule;

import io.github.lucasssoh.boussole.format.diagnostic.Code;
import io.github.lucasssoh.boussole.format.diagnostic.Diagnostics;
import io.github.lucasssoh.boussole.format.model.LearnerProfile;
import io.github.lucasssoh.boussole.format.model.block.Block;
import io.github.lucasssoh.boussole.format.model.block.Example;
import io.github.lucasssoh.boussole.format.model.block.Qcm;
import io.github.lucasssoh.boussole.format.model.part.Notion;
import io.github.lucasssoh.boussole.format.validate.ValidationContext;
import io.github.lucasssoh.boussole.format.validate.ValidationRule;

/**
 * P007: when the learner asked for {@code quick_check: true}, each notion asks a short qcm right
 * after its explanation, before its example.
 */
public final class QuickCheckRule implements ValidationRule {

    @Override
    public void check(ValidationContext context, Diagnostics diagnostics) {
        if (!context.course().learner().map(LearnerProfile::quickCheck).orElse(false)) {
            return;
        }
        Notions.of(context.course())
                .filter(notion -> !hasQuickCheck(notion))
                .forEach(notion -> diagnostics.report(Code.P007, notion.position(), "notion `" + notion.id()
                        + "`: the learner asked for a short qcm right after the explanation, before the example"));
    }

    private static boolean hasQuickCheck(Notion notion) {
        for (Block block : notion.blocks()) {
            if (block instanceof Qcm) {
                return true;
            }
            if (block instanceof Example) {
                return false;
            }
        }
        return false;
    }
}
