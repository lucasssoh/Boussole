package io.github.lucasssoh.boussole.format.validate.rule;

import io.github.lucasssoh.boussole.format.diagnostic.Code;
import io.github.lucasssoh.boussole.format.diagnostic.Diagnostics;
import io.github.lucasssoh.boussole.format.model.Course;
import io.github.lucasssoh.boussole.format.model.LoadedSession;
import io.github.lucasssoh.boussole.format.model.SourceRef;
import io.github.lucasssoh.boussole.format.model.block.PdfMention;
import io.github.lucasssoh.boussole.format.model.block.PdfRef;
import io.github.lucasssoh.boussole.format.model.block.Prose;
import io.github.lucasssoh.boussole.format.source.SourceDocuments;
import io.github.lucasssoh.boussole.format.validate.ValidationContext;
import io.github.lucasssoh.boussole.format.validate.ValidationRule;
import java.util.OptionalInt;
import java.util.stream.Stream;

/** F031: every source named exists in the sources folder, and every page is in its PDF. */
public final class SourceFileRule implements ValidationRule {

    @Override
    public void check(ValidationContext context, Diagnostics diagnostics) {
        Course course = context.course();
        Stream<SourceRef> fromSyllabus = course.syllabus().chapters().stream().flatMap(chapter -> chapter.sources().stream());
        Stream<SourceRef> fromSessions = course.sessions().stream().flatMap(SourceFileRule::sources);
        Stream.concat(fromSyllabus, fromSessions).forEach(source -> check(source, context.sources(), course, diagnostics));
    }

    private static Stream<SourceRef> sources(LoadedSession loaded) {
        Stream<SourceRef> header = loaded.session().header().sources().stream();
        Stream<SourceRef> blocks = loaded.session().blocks().flatMap(block -> switch (block) {
            case PdfRef pdf -> Stream.of(pdf.source());
            case Prose prose -> prose.inlines().stream()
                    .filter(PdfMention.class::isInstance)
                    .map(mention -> ((PdfMention) mention).source());
            default -> Stream.empty();
        });
        return Stream.concat(header, blocks);
    }

    private static void check(SourceRef source, SourceDocuments documents, Course course, Diagnostics diagnostics) {
        if (!documents.exists(source.file())) {
            diagnostics.report(Code.F031, source.position(), "`" + source.file() + "` is not in "
                    + course.subject().sourcesDir() + "/");
            return;
        }
        OptionalInt pages = documents.pageCount(source.file());
        if (source.page().isPresent() && pages.isPresent() && source.page().getAsInt() > pages.getAsInt()) {
            diagnostics.report(Code.F031, source.position(), "`" + source.file() + "` has " + pages.getAsInt()
                    + " pages, not " + source.page().getAsInt());
        }
    }
}
