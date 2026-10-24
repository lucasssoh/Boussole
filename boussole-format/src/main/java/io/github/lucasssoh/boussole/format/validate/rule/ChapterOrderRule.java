package io.github.lucasssoh.boussole.format.validate.rule;

import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCode;
import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCollector;
import io.github.lucasssoh.boussole.format.model.ChapterLinks;
import io.github.lucasssoh.boussole.format.model.Course;
import io.github.lucasssoh.boussole.format.model.SyllabusChapter;
import io.github.lucasssoh.boussole.format.validate.ValidationContext;
import io.github.lucasssoh.boussole.format.validate.ValidationRule;
import java.util.List;
import java.util.stream.Collectors;

/**
 * F044: the chapters follow one order. A chapter waits for every chapter one of its sessions has a
 * prerequisite in ({@link ChapterLinks}); chapters that wait for one another could never open.
 */
public final class ChapterOrderRule implements ValidationRule {

    @Override
    public void check(ValidationContext context, DiagnosticCollector diagnostics) {
        Course course = context.course();
        ChapterLinks.of(course).loop().ifPresent(loop -> {
            SyllabusChapter first = course.syllabus().chapters().stream()
                    .filter(chapter -> chapter.id().equals(loop.getFirst()))
                    .findFirst()
                    .orElseThrow();
            diagnostics.report(DiagnosticCode.F044, first.position(), "chapters wait for one another: "
                    + quoted(loop) + "; a chapter is finished before the chapters that need it, so move a session"
                    + " or a prerequisite until the chapters follow one order");
        });
    }

    private static String quoted(List<String> loop) {
        return loop.stream().map(chapter -> "`" + chapter + "`").collect(Collectors.joining(" waits for "));
    }
}
