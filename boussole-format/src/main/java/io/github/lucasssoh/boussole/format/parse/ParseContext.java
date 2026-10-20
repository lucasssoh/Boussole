package io.github.lucasssoh.boussole.format.parse;

import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCode;
import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCollector;
import io.github.lucasssoh.boussole.format.diagnostic.SourcePosition;
import io.github.lucasssoh.boussole.format.model.block.Block;
import io.github.lucasssoh.boussole.format.parse.answer.AnswerParserRegistry;
import io.github.lucasssoh.boussole.format.parse.directive.DirectiveParserRegistry;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

/** What the parsers of one session file share: where they are, where to report, how to parse nested content. */
public final class ParseContext {

    private final String file;
    private final DiagnosticCollector diagnostics;
    private final BlockBuilder blockBuilder;
    private final AnswerParserRegistry answerParsers = AnswerParserRegistry.standard();

    public ParseContext(String file, DiagnosticCollector diagnostics) {
        this.file = file;
        this.diagnostics = diagnostics;
        this.blockBuilder = new BlockBuilder(this, DirectiveParserRegistry.standard());
    }

    public SourcePosition position(int line) {
        return new SourcePosition(file, line);
    }

    public void report(DiagnosticCode code, int line, String message) {
        diagnostics.report(code, position(line), message);
    }

    /** The blocks of {@code nodes}, found inside the directive {@code parent} ({@code ""} for the session body). */
    public List<Block> blocks(List<RawNode> nodes, String parent) {
        return blockBuilder.build(nodes, parent);
    }

    public AnswerParserRegistry answerParsers() {
        return answerParsers;
    }

    /**
     * Reads the attributes of a directive and checks their names. Unknown attributes are
     * reported; a missing required one makes the result empty.
     */
    public Optional<DirectiveAttributes> attributes(
            String directive, String source, Set<String> required, Set<String> optional, int line) {
        DirectiveAttributes attributes;
        try {
            attributes = DirectiveAttributeParser.parse(source);
        } catch (InvalidAttributeException e) {
            report(DiagnosticCode.F027, line, "attributes of `" + directive + "`: " + e.getMessage());
            return Optional.empty();
        }
        for (String name : new TreeSet<>(attributes.names())) {
            if (!required.contains(name) && !optional.contains(name)) {
                report(DiagnosticCode.F027, line, "`" + name + "` is not an attribute of `" + directive + "`");
            }
        }
        Set<String> missing = new TreeSet<>(required);
        missing.removeAll(attributes.names());
        missing.forEach(name -> report(DiagnosticCode.F027, line, "`" + directive + "` needs the attribute `" + name + "`"));
        return missing.isEmpty() ? Optional.of(attributes) : Optional.empty();
    }
}
