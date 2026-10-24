package io.github.lucasssoh.boussole.format.validate.rule;

import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCode;
import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCollector;
import io.github.lucasssoh.boussole.format.model.CourseSession;
import io.github.lucasssoh.boussole.format.model.block.Block;
import io.github.lucasssoh.boussole.format.model.block.BlockWalker;
import io.github.lucasssoh.boussole.format.model.block.LinkBlock;
import io.github.lucasssoh.boussole.format.model.block.LinkInlineDirective;
import io.github.lucasssoh.boussole.format.model.block.PdfBlock;
import io.github.lucasssoh.boussole.format.model.block.PdfInlineDirective;
import io.github.lucasssoh.boussole.format.model.block.ProseBlock;
import io.github.lucasssoh.boussole.format.model.part.NotionPart;
import io.github.lucasssoh.boussole.format.model.part.Part;
import io.github.lucasssoh.boussole.format.validate.ValidationContext;
import io.github.lucasssoh.boussole.format.validate.ValidationRule;

/**
 * P014: in a track built from documents, every notion points to where what it says comes from: a
 * page of the documents ({@code :pdf}) or a web source ({@code :link}). What comes from elsewhere
 * goes in a {@code :::beyond}, which says where.
 */
public final class CitationRule implements ValidationRule {

    @Override
    public void check(ValidationContext context, DiagnosticCollector diagnostics) {
        if (context.sources().documents().isEmpty()) {
            return;
        }
        for (CourseSession loaded : context.course().sessions()) {
            for (Part part : loaded.session().parts()) {
                if (part instanceof NotionPart notion && BlockWalker.walk(notion.blocks()).noneMatch(CitationRule::cites)) {
                    diagnostics.report(DiagnosticCode.P014, notion.position(), "notion `" + notion.id() + "` cites no source: point to"
                            + " the page it relies on with :pdf[…]{src=… page=…}, or :link[…]{url=…} for a web source, and put what"
                            + " comes from elsewhere in a :::beyond that says where");
                }
            }
        }
    }

    private static boolean cites(Block block) {
        return switch (block) {
            case PdfBlock _, LinkBlock _ -> true;
            case ProseBlock prose -> prose.inlines().stream()
                    .anyMatch(inline -> inline instanceof PdfInlineDirective || inline instanceof LinkInlineDirective);
            default -> false;
        };
    }
}
