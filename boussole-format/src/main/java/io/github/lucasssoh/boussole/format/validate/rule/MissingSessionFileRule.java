package io.github.lucasssoh.boussole.format.validate.rule;

import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCode;
import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCollector;
import io.github.lucasssoh.boussole.format.model.SyllabusChapter;
import io.github.lucasssoh.boussole.format.model.Course;
import io.github.lucasssoh.boussole.format.model.SyllabusSession;
import io.github.lucasssoh.boussole.format.model.SyllabusStatus;
import io.github.lucasssoh.boussole.format.validate.ValidationContext;
import io.github.lucasssoh.boussole.format.validate.ValidationRule;

/** F011: once the syllabus is approved, each of its sessions is expected to have a file. */
public final class MissingSessionFileRule implements ValidationRule {

    @Override
    public void check(ValidationContext context, DiagnosticCollector diagnostics) {
        Course course = context.course();
        if (course.syllabus().status() != SyllabusStatus.APPROVED) {
            return;
        }
        for (SyllabusChapter chapter : course.syllabus().chapters()) {
            for (SyllabusSession entry : chapter.sessions()) {
                boolean written = course.sessions().stream().anyMatch(loaded -> loaded.entry() == entry);
                if (!written) {
                    diagnostics.report(DiagnosticCode.F011, entry.position(), "session `" + entry.id() + "` is not written yet: cursus/"
                            + chapter.dir() + "/" + entry.file());
                }
            }
        }
    }
}
