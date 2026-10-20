package io.github.lucasssoh.boussole.format.parse.directive;

import io.github.lucasssoh.boussole.format.model.SourceRef;
import io.github.lucasssoh.boussole.format.model.block.Block;
import io.github.lucasssoh.boussole.format.model.block.PdfRef;
import io.github.lucasssoh.boussole.format.parse.ParseContext;
import java.util.Optional;
import java.util.Set;

/** {@code ::pdf[label]{src=… page=…}}: a source document opened beside the text. */
final class PdfParser implements DirectiveParser {

    private static final DirectiveSpec SPEC =
            new DirectiveSpec("pdf", DirectiveKind.LEAF, Set.of("src"), Set.of("page"), Parents.MEDIA);

    @Override
    public DirectiveSpec spec() {
        return SPEC;
    }

    @Override
    public Optional<Block> parse(Directive directive, ParseContext context) {
        SourceRef source = new SourceRef(
                directive.attributes().required("src"),
                directive.attributes().positiveInteger("page"),
                context.position(directive.line()));
        return Optional.of(new PdfRef(directive.label(), source));
    }
}
