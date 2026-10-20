package io.github.lucasssoh.boussole.format.validate.rule;

import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCode;
import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCollector;
import io.github.lucasssoh.boussole.format.model.Course;
import io.github.lucasssoh.boussole.format.model.CourseSession;
import io.github.lucasssoh.boussole.format.model.SourceReference;
import io.github.lucasssoh.boussole.format.model.block.PdfInlineDirective;
import io.github.lucasssoh.boussole.format.model.block.PdfBlock;
import io.github.lucasssoh.boussole.format.model.block.ProseBlock;
import io.github.lucasssoh.boussole.format.source.SourceDocuments;
import io.github.lucasssoh.boussole.format.validate.ValidationContext;
import io.github.lucasssoh.boussole.format.validate.ValidationRule;
import java.util.OptionalInt;
import java.util.stream.Stream;

/** F031: every source named exists in the sources folder, and every page is in its PDF. */
public final class SourceFileRule implements ValidationRule {

    @Override
    public void check(ValidationContext context, DiagnosticCollector diagnostics) {
        Course course = context.course();
        Stream<SourceReference> fromSyllabus = course.syllabus().chapters().stream().flatMap(chapter -> chapter.sources().stream());
        Stream<SourceReference> fromSessions = course.sessions().stream().flatMap(SourceFileRule::sources);
        Stream.concat(fromSyllabus, fromSessions).forEach(source -> check(source, context.sources(), course, diagnostics));
    }

    private static Stream<SourceReference> sources(CourseSession loaded) {
        Stream<SourceReference> header = loaded.session().header().sources().stream();
        Stream<SourceReference> blocks = loaded.session().blocks().flatMap(block -> switch (block) {
            case PdfBlock pdf -> Stream.of(pdf.source());
            case ProseBlock prose -> prose.inlines().stream()
                    .filter(PdfInlineDirective.class::isInstance)
                    .map(mention -> ((PdfInlineDirective) mention).source());
            default -> Stream.empty();
        });
        return Stream.concat(header, blocks);
    }

    private static void check(SourceReference source, SourceDocuments documents, Course course, DiagnosticCollector diagnostics) {
        if (!documents.exists(source.file())) {
            diagnostics.report(DiagnosticCode.F031, source.position(), "`" + source.file() + "` is not in "
                    + course.subject().sourcesDir() + "/");
            return;
        }
        OptionalInt pages = documents.pageCount(source.file());
        if (source.page().isPresent() && pages.isPresent() && source.page().getAsInt() > pages.getAsInt()) {
            diagnostics.report(DiagnosticCode.F031, source.position(), "`" + source.file() + "` has " + pages.getAsInt()
                    + " pages, not " + source.page().getAsInt());
        }
    }
}
