package io.github.lucasssoh.boussole.format.parse;

import io.github.lucasssoh.boussole.format.diagnostic.Code;
import io.github.lucasssoh.boussole.format.diagnostic.Diagnostics;
import io.github.lucasssoh.boussole.format.diagnostic.Position;
import io.github.lucasssoh.boussole.format.model.block.Block;
import io.github.lucasssoh.boussole.format.parse.answer.AnswerParsers;
import io.github.lucasssoh.boussole.format.parse.directive.DirectiveRegistry;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

/** What the parsers of one session file share: where they are, where to report, how to parse nested content. */
public final class ParseContext {

    private final String file;
    private final Diagnostics diagnostics;
    private final BlockBuilder blockBuilder;
    private final AnswerParsers answerParsers = AnswerParsers.standard();

    public ParseContext(String file, Diagnostics diagnostics) {
        this.file = file;
        this.diagnostics = diagnostics;
        this.blockBuilder = new BlockBuilder(this, DirectiveRegistry.standard());
    }

    public Position position(int line) {
        return new Position(file, line);
    }

    public void report(Code code, int line, String message) {
        diagnostics.report(code, position(line), message);
    }

    /** The blocks of {@code nodes}, found inside the directive {@code parent} ({@code ""} for the session body). */
    public List<Block> blocks(List<RawNode> nodes, String parent) {
        return blockBuilder.build(nodes, parent);
    }

    public AnswerParsers answerParsers() {
        return answerParsers;
    }

    /**
     * Reads the attributes of a directive and checks their names. Unknown attributes are
     * reported; a missing required one makes the result empty.
     */
    public Optional<Attributes> attributes(
            String directive, String source, Set<String> required, Set<String> optional, int line) {
        Attributes attributes;
        try {
            attributes = AttributeParser.parse(source);
        } catch (InvalidAttributeException e) {
            report(Code.F027, line, "attributes of `" + directive + "`: " + e.getMessage());
            return Optional.empty();
        }
        for (String name : new TreeSet<>(attributes.names())) {
            if (!required.contains(name) && !optional.contains(name)) {
                report(Code.F027, line, "`" + name + "` is not an attribute of `" + directive + "`");
            }
        }
        Set<String> missing = new TreeSet<>(required);
        missing.removeAll(attributes.names());
        missing.forEach(name -> report(Code.F027, line, "`" + directive + "` needs the attribute `" + name + "`"));
        return missing.isEmpty() ? Optional.of(attributes) : Optional.empty();
    }
}
