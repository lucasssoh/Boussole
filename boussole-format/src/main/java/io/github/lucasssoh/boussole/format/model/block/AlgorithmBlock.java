package io.github.lucasssoh.boussole.format.model.block;

import io.github.lucasssoh.boussole.format.diagnostic.SourcePosition;
import java.util.List;

/**
 * An algorithm in pseudo-code, one line per step, its indentation giving the structure. The
 * interface numbers the lines, so that a question may point to « la ligne 3 ».
 */
public record AlgorithmBlock(String label, List<AlgorithmLine> lines, SourcePosition position) implements Block {

    public AlgorithmBlock {
        lines = List.copyOf(lines);
    }
}
