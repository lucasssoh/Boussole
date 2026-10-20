package io.github.lucasssoh.boussole.format.validate.rule;

import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCode;
import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCollector;
import io.github.lucasssoh.boussole.format.model.block.McqBlock;
import io.github.lucasssoh.boussole.format.validate.ValidationContext;
import io.github.lucasssoh.boussole.format.validate.ValidationRule;

/**
 * P010: each wrong choice of an MCQ says why it is wrong, since the learner who picks it sees
 * it in red right after answering.
 */
public final class McqFeedbackRule implements ValidationRule {

    @Override
    public void check(ValidationContext context, DiagnosticCollector diagnostics) {
        context.course().blocks()
                .filter(McqBlock.class::isInstance)
                .map(McqBlock.class::cast)
                .forEach(mcq -> mcq.choices().stream()
                        .filter(choice -> !choice.correct() && choice.feedback().isEmpty())
                        .forEach(choice -> diagnostics.report(DiagnosticCode.P010, choice.position(),
                                "wrong choice of `" + mcq.id() + "` without feedback: add `  > why it is wrong` under it")));
    }
}
