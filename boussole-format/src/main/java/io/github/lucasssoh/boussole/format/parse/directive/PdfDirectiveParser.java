package io.github.lucasssoh.boussole.format.parse.directive;

import io.github.lucasssoh.boussole.format.model.SourceReference;
import io.github.lucasssoh.boussole.format.model.block.Block;
import io.github.lucasssoh.boussole.format.model.block.PdfBlock;
import io.github.lucasssoh.boussole.format.parse.ParseContext;
import java.util.Optional;
import java.util.Set;

/** {@code ::pdf[label]{src=… page=…}}: a source document opened beside the text. */
final class PdfDirectiveParser implements DirectiveParser {

    private static final DirectiveSpec SPEC =
            new DirectiveSpec("pdf", DirectiveKind.LEAF, Set.of("src"), Set.of("page"), DirectivePlaces.MEDIA);

    @Override
    public DirectiveSpec spec() {
        return SPEC;
    }

    @Override
    public Optional<Block> parse(CheckedDirective directive, ParseContext context) {
        SourceReference source = new SourceReference(
                directive.attributes().required("src"),
                directive.attributes().positiveInteger("page"),
                context.position(directive.line()));
        return Optional.of(new PdfBlock(directive.label(), source));
    }
}
