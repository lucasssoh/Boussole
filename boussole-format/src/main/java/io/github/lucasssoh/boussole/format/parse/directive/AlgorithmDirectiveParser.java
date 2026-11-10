package io.github.lucasssoh.boussole.format.parse.directive;

import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCode;
import io.github.lucasssoh.boussole.format.model.block.AlgorithmBlock;
import io.github.lucasssoh.boussole.format.model.block.AlgorithmLine;
import io.github.lucasssoh.boussole.format.model.block.Block;
import io.github.lucasssoh.boussole.format.parse.ParseContext;
import io.github.lucasssoh.boussole.format.parse.RawNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * {@code :::algorithm[title]}: pseudo-code, one step per line, indented to show what a loop or a
 * test holds. Blank lines are left out. The smallest indentation step found is one level.
 *
 * <pre>
 * Entrée : $m$ machines, les tâches $J_1, …, J_n$
 * pour $j$ de 1 à $n$ faire
 *   placer $J_j$ sur la machine la moins chargée   // la première en cas d'égalité
 * retourner l'ordonnancement
 * </pre>
 */
final class AlgorithmDirectiveParser implements DirectiveParser {

    private static final DirectiveSpec SPEC =
            new DirectiveSpec("algorithm", DirectiveKind.CONTAINER, Set.of(), Set.of(), DirectivePlaces.MEDIA);
    private static final int TAB = 4;

    @Override
    public DirectiveSpec spec() {
        return SPEC;
    }

    @Override
    public Optional<Block> parse(CheckedDirective directive, ParseContext context) {
        List<String> texts = new ArrayList<>();
        List<Integer> widths = new ArrayList<>();
        boolean failed = false;
        for (RawNode child : directive.children()) {
            if (child instanceof RawNode.Text text) {
                for (String line : text.lines()) {
                    if (!line.isBlank()) {
                        widths.add(width(line));
                        texts.add(line.strip());
                    }
                }
            } else {
                failed = true;
                context.report(DiagnosticCode.F049, child.line(),
                        "`algorithm` holds only lines of pseudo-code: write this step as a line, and put any other block after the algorithm");
            }
        }
        if (texts.isEmpty()) {
            if (!failed) {
                context.report(DiagnosticCode.F049, directive.line(),
                        "`algorithm` is empty: write one step per line, indented under the loop or test that holds it");
            }
            return Optional.empty();
        }
        if (failed) {
            return Optional.empty();
        }
        int base = widths.stream().mapToInt(Integer::intValue).min().orElse(0);
        int step = widths.stream().mapToInt(width -> width - base).filter(width -> width > 0).min().orElse(1);
        List<AlgorithmLine> lines = new ArrayList<>();
        for (int index = 0; index < texts.size(); index++) {
            lines.add(new AlgorithmLine((widths.get(index) - base) / step, texts.get(index)));
        }
        return Optional.of(new AlgorithmBlock(directive.label(), lines, context.position(directive.line())));
    }

    /** The indentation of a line, a tab counting as {@value #TAB} spaces. */
    private static int width(String line) {
        int width = 0;
        for (char c : line.toCharArray()) {
            if (c == ' ') {
                width++;
            } else if (c == '\t') {
                width += TAB;
            } else {
                break;
            }
        }
        return width;
    }
}
