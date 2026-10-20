package io.github.lucasssoh.boussole.format.validate.rule;

import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCode;
import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCollector;
import io.github.lucasssoh.boussole.format.model.block.ExampleBlock;
import io.github.lucasssoh.boussole.format.validate.ValidationContext;
import io.github.lucasssoh.boussole.format.validate.ValidationRule;

/** P002: one worked example per notion, then the learner tries. */
public final class SingleExampleRule implements ValidationRule {

    @Override
    public void check(ValidationContext context, DiagnosticCollector diagnostics) {
        LessonNotions.of(context.course()).forEach(notion -> {
            long examples = notion.blocks().stream().filter(ExampleBlock.class::isInstance).count();
            if (examples > 1) {
                diagnostics.report(DiagnosticCode.P002, notion.position(), "notion `" + notion.id() + "` has " + examples
                        + " examples: keep one, and turn the others into exercises");
            }
        });
    }
}
