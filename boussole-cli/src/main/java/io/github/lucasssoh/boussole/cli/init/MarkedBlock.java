package io.github.lucasssoh.boussole.cli.init;

import java.util.Optional;

/**
 * A block of text Boussole owns inside a file someone else may also edit, such as
 * {@code AGENTS.md}. It sits between two marker lines; everything outside them is left as is.
 */
final class MarkedBlock {

    static final String START = "<!-- boussole:start (written by boussole init, changes here are replaced) -->";
    static final String END = "<!-- boussole:end -->";

    private MarkedBlock() {
    }

    /** The file's text with the block replaced, or added at the end when the file has none. */
    static String upsert(Optional<String> file, String block) {
        String marked = START + "\n" + block.strip() + "\n" + END + "\n";
        if (file.isEmpty() || file.get().isBlank()) {
            return marked;
        }
        String text = file.get();
        int start = text.indexOf(START);
        int end = text.indexOf(END);
        if (start >= 0 && end > start) {
            int after = end + END.length();
            if (after < text.length() && text.charAt(after) == '\n') {
                after++;
            }
            return text.substring(0, start) + marked + text.substring(after);
        }
        return (text.endsWith("\n") ? text : text + "\n") + "\n" + marked;
    }
}
