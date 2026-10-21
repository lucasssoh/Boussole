package io.github.lucasssoh.boussole.format.validate;

import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCollector;
import io.github.lucasssoh.boussole.format.validate.rule.ClosingRule;
import io.github.lucasssoh.boussole.format.validate.rule.DeclaredTermsRule;
import io.github.lucasssoh.boussole.format.validate.rule.DuplicateIdRule;
import io.github.lucasssoh.boussole.format.validate.rule.FillGapsRule;
import io.github.lucasssoh.boussole.format.validate.rule.AuthorshipRule;
import io.github.lucasssoh.boussole.format.validate.rule.HintPresenceRule;
import io.github.lucasssoh.boussole.format.validate.rule.IdSyntaxRule;
import io.github.lucasssoh.boussole.format.validate.rule.MissingSessionFileRule;
import io.github.lucasssoh.boussole.format.validate.rule.NotionExerciseRule;
import io.github.lucasssoh.boussole.format.validate.rule.PartOrderRule;
import io.github.lucasssoh.boussole.format.validate.rule.McqChoicesRule;
import io.github.lucasssoh.boussole.format.validate.rule.McqFeedbackRule;
import io.github.lucasssoh.boussole.format.validate.rule.QuickCheckRule;
import io.github.lucasssoh.boussole.format.validate.rule.ReferenceRule;
import io.github.lucasssoh.boussole.format.validate.rule.SessionIdMatchRule;
import io.github.lucasssoh.boussole.format.validate.rule.SingleExampleRule;
import io.github.lucasssoh.boussole.format.validate.rule.SourceFileRule;
import io.github.lucasssoh.boussole.format.validate.rule.SourceInventoryRule;
import io.github.lucasssoh.boussole.format.validate.rule.SourceProvenanceRule;
import io.github.lucasssoh.boussole.format.validate.rule.TimedSessionContentRule;
import io.github.lucasssoh.boussole.format.validate.rule.TimedSessionFitRule;
import io.github.lucasssoh.boussole.format.validate.rule.VideoExistsRule;
import java.util.List;

/**
 * Runs every rule on a course. The problems a single file shows on its own are reported while
 * it is read (see {@link io.github.lucasssoh.boussole.format.load.CourseLoader}); the rules
 * check what needs the whole course.
 */
public final class Validator {

    private final List<ValidationRule> rules;

    public Validator(List<ValidationRule> rules) {
        this.rules = List.copyOf(rules);
    }

    public static Validator standard() {
        return new Validator(List.of(
                new DuplicateIdRule(),
                new IdSyntaxRule(),
                new SessionIdMatchRule(),
                new MissingSessionFileRule(),
                new McqChoicesRule(),
                new FillGapsRule(),
                new ReferenceRule(),
                new SourceFileRule(),
                new SourceInventoryRule(),
                new SourceProvenanceRule(),
                new VideoExistsRule(),
                new NotionExerciseRule(),
                new SingleExampleRule(),
                new PartOrderRule(),
                new HintPresenceRule(),
                new AuthorshipRule(),
                new McqFeedbackRule(),
                new DeclaredTermsRule(),
                new ClosingRule(),
                new QuickCheckRule(),
                new TimedSessionFitRule(),
                new TimedSessionContentRule()));
    }

    public void validate(ValidationContext context, DiagnosticCollector diagnostics) {
        rules.forEach(rule -> rule.check(context, diagnostics));
    }
}
