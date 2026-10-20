package io.github.lucasssoh.boussole.format.parse;

import io.github.lucasssoh.boussole.format.diagnostic.Diagnostics;
import io.github.lucasssoh.boussole.format.model.part.Part;
import java.util.List;

/** Parses the body of a session file, the part after its front matter. */
public final class SessionBodyParser {

    private SessionBodyParser() {
    }

    /**
     * @param lines     the body's lines
     * @param firstLine the line number of the first of them in the file
     * @param file      the file, relative to the subject folder, for diagnostics
     */
    public static List<Part> parse(List<String> lines, int firstLine, String file, Diagnostics diagnostics) {
        ParseContext context = new ParseContext(file, diagnostics);
        List<RawNode> nodes = new RawTreeBuilder(context).build(lines, firstLine);
        return new PartAssembler(context).assemble(nodes);
    }
}
