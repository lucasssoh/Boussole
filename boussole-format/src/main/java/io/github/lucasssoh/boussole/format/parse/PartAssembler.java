package io.github.lucasssoh.boussole.format.parse;

import io.github.lucasssoh.boussole.format.diagnostic.Code;
import io.github.lucasssoh.boussole.format.model.block.Block;
import io.github.lucasssoh.boussole.format.model.part.Checkpoint;
import io.github.lucasssoh.boussole.format.model.part.Notion;
import io.github.lucasssoh.boussole.format.model.part.Part;
import io.github.lucasssoh.boussole.format.model.part.Practice;
import io.github.lucasssoh.boussole.format.model.part.TextPart;
import io.github.lucasssoh.boussole.format.model.part.Warmup;
import io.github.lucasssoh.boussole.format.parse.directive.Parents;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Groups the top-level nodes of a session into parts: a level-2 heading starts a notion, a
 * {@code warmup}, {@code checkpoint} or {@code practice} container is a part of its own, and
 * anything else belongs to the notion in progress, or to a text part when there is none.
 */
final class PartAssembler {

    private final ParseContext context;
    private final List<Part> parts = new ArrayList<>();
    private OpenPart current;

    PartAssembler(ParseContext context) {
        this.context = context;
    }

    List<Part> assemble(List<RawNode> nodes) {
        for (RawNode node : nodes) {
            switch (node) {
                case RawNode.Heading heading -> startNotion(heading);
                case RawNode.Container container when Parents.SECTIONS.contains(container.directive().name()) ->
                        addSection(container);
                default -> append(context.blocks(List.of(node), Parents.TOP), node.line());
            }
        }
        finishCurrent();
        return List.copyOf(parts);
    }

    private void startNotion(RawNode.Heading heading) {
        finishCurrent();
        if (heading.id().isEmpty()) {
            context.report(Code.F013, heading.line(), "`## " + heading.title() + "` needs an ID: `## "
                    + heading.title() + " {#an-id}`");
        }
        current = new OpenPart(heading.id().orElse(""), heading.title(), heading.line(), true);
    }

    private void addSection(RawNode.Container container) {
        finishCurrent();
        String name = container.directive().name();
        context.attributes(name, container.directive().attributes(), Set.of(), Set.of(), container.line());
        List<Block> blocks = context.blocks(container.children(), name);
        var position = context.position(container.line());
        parts.add(switch (name) {
            case "warmup" -> new Warmup(blocks, position);
            case "checkpoint" -> new Checkpoint(blocks, position);
            default -> new Practice(blocks, position);
        });
    }

    private void append(List<Block> blocks, int line) {
        if (blocks.isEmpty()) {
            return;
        }
        if (current == null) {
            current = new OpenPart("", "", line, false);
        }
        current.blocks.addAll(blocks);
    }

    private void finishCurrent() {
        if (current == null) {
            return;
        }
        var position = context.position(current.line);
        parts.add(current.notion
                ? new Notion(current.id, current.title, List.copyOf(current.blocks), position)
                : new TextPart(List.copyOf(current.blocks), position));
        current = null;
    }

    private static final class OpenPart {

        private final String id;
        private final String title;
        private final int line;
        private final boolean notion;
        private final List<Block> blocks = new ArrayList<>();

        private OpenPart(String id, String title, int line, boolean notion) {
            this.id = id;
            this.title = title;
            this.line = line;
            this.notion = notion;
        }
    }
}
