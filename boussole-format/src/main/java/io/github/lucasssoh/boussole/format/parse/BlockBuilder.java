package io.github.lucasssoh.boussole.format.parse;

import io.github.lucasssoh.boussole.format.diagnostic.Code;
import io.github.lucasssoh.boussole.format.model.block.Block;
import io.github.lucasssoh.boussole.format.parse.directive.Directive;
import io.github.lucasssoh.boussole.format.parse.directive.DirectiveKind;
import io.github.lucasssoh.boussole.format.parse.directive.DirectiveParser;
import io.github.lucasssoh.boussole.format.parse.directive.DirectiveRegistry;
import io.github.lucasssoh.boussole.format.parse.directive.DirectiveSpec;
import io.github.lucasssoh.boussole.format.parse.directive.Parents;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Turns raw nodes into blocks: text becomes {@link io.github.lucasssoh.boussole.format.model.block.Prose},
 * each directive goes to its parser from the {@link DirectiveRegistry}, once its kind, its place and its
 * attributes are checked.
 */
final class BlockBuilder {

    private final ParseContext context;
    private final DirectiveRegistry registry;
    private final InlineScanner inlineScanner;

    BlockBuilder(ParseContext context, DirectiveRegistry registry) {
        this.context = context;
        this.registry = registry;
        this.inlineScanner = new InlineScanner(context);
    }

    List<Block> build(List<RawNode> nodes, String parent) {
        List<Block> blocks = new ArrayList<>();
        for (RawNode node : nodes) {
            switch (node) {
                case RawNode.Text text when text.isBlank() -> { }
                case RawNode.Text text -> blocks.add(inlineScanner.prose(text));
                case RawNode.Heading heading -> blocks.add(inlineScanner.prose(
                        new RawNode.Text(List.of("## " + heading.title()), heading.line())));
                case RawNode.Leaf leaf -> directive(leaf.directive(), List.of(), leaf.line(), DirectiveKind.LEAF, parent)
                        .ifPresent(blocks::add);
                case RawNode.Container container -> directive(
                                container.directive(), container.children(), container.line(), DirectiveKind.CONTAINER, parent)
                        .ifPresent(blocks::add);
            }
        }
        return List.copyOf(blocks);
    }

    private Optional<Block> directive(
            DirectiveLine line, List<RawNode> children, int number, DirectiveKind kind, String parent) {
        String name = line.name();
        if (Parents.SECTIONS.contains(name)) {
            context.report(Code.F020, number, "`" + name + "` is only allowed at the top level of a session");
            return Optional.empty();
        }
        if (Parents.EXERCISE_PARTS.contains(name)) {
            context.report(Code.F020, number, "`" + name + "` is only allowed inside `exercise` or `qcm`, as :::" + name);
            return Optional.empty();
        }
        Optional<DirectiveParser> parser = registry.find(name);
        if (parser.isEmpty()) {
            context.report(Code.F020, number, "unknown directive `" + name + "`");
            return Optional.empty();
        }
        DirectiveSpec spec = parser.get().spec();
        if (spec.kind() != kind) {
            context.report(Code.F020, number, spec.kind() == DirectiveKind.LEAF
                    ? "`" + name + "` is a one-line directive: ::" + name
                    : "`" + name + "` is a container: :::" + name + " … :::");
            return Optional.empty();
        }
        if (!spec.parents().contains(parent)) {
            context.report(Code.F020, number, "`" + name + "` is not allowed in " + Parents.describe(parent));
            return Optional.empty();
        }
        return context.attributes(name, line.attributes(), spec.required(), spec.optional(), number)
                .flatMap(attributes -> parse(parser.get(), new Directive(name, line.label(), attributes, children, number)));
    }

    private Optional<Block> parse(DirectiveParser parser, Directive directive) {
        try {
            return parser.parse(directive, context);
        } catch (InvalidAttributeException e) {
            context.report(Code.F027, directive.line(), "`" + directive.name() + "`: " + e.getMessage());
            return Optional.empty();
        }
    }
}
