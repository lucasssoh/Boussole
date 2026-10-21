package io.github.lucasssoh.boussole.format.parse.directive;

import io.github.lucasssoh.boussole.format.model.block.Block;
import io.github.lucasssoh.boussole.format.model.block.LinkBlock;
import io.github.lucasssoh.boussole.format.parse.ParseContext;
import java.util.Optional;
import java.util.Set;

/** {@code ::link[label]{url=…}}: a web page cited by the course, opened beside the text. */
final class LinkDirectiveParser implements DirectiveParser {

    private static final DirectiveSpec SPEC =
            new DirectiveSpec("link", DirectiveKind.LEAF, Set.of("url"), Set.of(), DirectivePlaces.MEDIA);

    @Override
    public DirectiveSpec spec() {
        return SPEC;
    }

    @Override
    public Optional<Block> parse(CheckedDirective directive, ParseContext context) {
        String url = WebAddress.read(directive.attributes().required("url"));
        return Optional.of(new LinkBlock(directive.label(), url, context.position(directive.line())));
    }
}
