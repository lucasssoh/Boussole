package io.github.lucasssoh.boussole.cli.admin;

import com.googlecode.lanterna.input.KeyStroke;
import java.util.List;

/** One screen of {@code boussole} alone, under its function key. */
interface AdminTab {

    String name();

    /** Reads again what the screen shows. Runs away from the screen, every second while it shows. */
    void refresh();

    /** Draws the screen in the rows from {@code top}, {@code height} of them. */
    void draw(Painter painter, int top, int height);

    /** Acts on a key; false when the screen has no use for it. */
    boolean key(KeyStroke key);

    /** The keys of the screen, for the bottom line. */
    List<Hint> hints();

    /** A key and what it does: {@code a Pair}. */
    record Hint(String key, String label) {
    }
}
