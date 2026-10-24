package io.github.lucasssoh.boussole.core.learn.reward;

import io.github.lucasssoh.boussole.format.model.block.Block;
import io.github.lucasssoh.boussole.format.model.block.ProseBlock;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The sentence each gap of a fill exercise sits in: a sentence ends with {@code .}, {@code ?} or
 * {@code !} followed by a space, at a blank line, at a new item of a list or a new row of a table;
 * a code block is one sentence. Formulas and inline code never end a sentence.
 */
final class GapSentences {

    private static final Pattern GAP = Pattern.compile("\\[\\[(\\d+)]]");
    private static final Pattern NEW_ITEM = Pattern.compile("\\n[ \\t]*([-*+]\\s|\\d+[.)]\\s|\\|)");

    private GapSentences() {
    }

    /** Gap number to sentence number. */
    static Map<Integer, Integer> of(List<Block> statement) {
        String text = statement.stream()
                .filter(ProseBlock.class::isInstance)
                .map(block -> ((ProseBlock) block).markdown())
                .collect(Collectors.joining("\n\n"));
        Map<Integer, Integer> sentenceOf = new HashMap<>();
        int sentence = 0;
        boolean math = false;
        boolean code = false;
        boolean fence = false;
        int index = 0;
        while (index < text.length()) {
            if (text.startsWith("```", index) && (index == 0 || text.charAt(index - 1) == '\n')) {
                fence = !fence;
                sentence++;
                index = lineEnd(text, index);
                continue;
            }
            Matcher gap = GAP.matcher(text).region(index, text.length());
            if (gap.lookingAt()) {
                sentenceOf.put(Integer.parseInt(gap.group(1)), sentence);
                index = gap.end();
                continue;
            }
            char c = text.charAt(index);
            if (!fence) {
                if (c == '`' && !math) {
                    code = !code;
                } else if (c == '$' && !code) {
                    math = !math;
                } else if (!math && !code) {
                    if ((c == '.' || c == '?' || c == '!') && (index + 1 == text.length() || Character.isWhitespace(text.charAt(index + 1)))) {
                        sentence++;
                    } else if (text.startsWith("\n\n", index) || NEW_ITEM.matcher(text).region(index, text.length()).lookingAt()) {
                        sentence++;
                    }
                }
            }
            index++;
        }
        return sentenceOf;
    }

    private static int lineEnd(String text, int from) {
        int end = text.indexOf('\n', from);
        return end < 0 ? text.length() : end + 1;
    }
}
