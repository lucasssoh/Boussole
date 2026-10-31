package io.github.lucasssoh.boussole.cli.admin;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import io.nayuki.qrcodegen.QrCode;
import java.nio.file.Path;
import java.util.EnumSet;

/**
 * Draws on the screen: text cut to its room, in a few styles, and framed panes. The colors are
 * the terminal's own sixteen, so that it reads on a light terminal as on a dark one.
 */
final class Painter {

    /** How a piece of text looks. */
    enum Style {
        PLAIN(TextColor.ANSI.DEFAULT, TextColor.ANSI.DEFAULT),
        DIM(TextColor.ANSI.BLACK_BRIGHT, TextColor.ANSI.DEFAULT),
        KEY(TextColor.ANSI.GREEN, TextColor.ANSI.DEFAULT, SGR.BOLD),
        GOOD(TextColor.ANSI.GREEN, TextColor.ANSI.DEFAULT),
        TITLE(TextColor.ANSI.YELLOW, TextColor.ANSI.DEFAULT, SGR.BOLD),
        WARNING(TextColor.ANSI.YELLOW, TextColor.ANSI.DEFAULT),
        ERROR(TextColor.ANSI.RED, TextColor.ANSI.DEFAULT),
        CODE(TextColor.ANSI.YELLOW, TextColor.ANSI.DEFAULT, SGR.BOLD),
        SELECTED(TextColor.ANSI.DEFAULT, TextColor.ANSI.DEFAULT, SGR.REVERSE),
        TAB(TextColor.ANSI.BLACK, TextColor.ANSI.GREEN, SGR.BOLD);

        private final TextColor foreground;
        private final TextColor background;
        private final EnumSet<SGR> modifiers;

        Style(TextColor foreground, TextColor background, SGR... modifiers) {
            this.foreground = foreground;
            this.background = background;
            this.modifiers = modifiers.length == 0 ? EnumSet.noneOf(SGR.class) : EnumSet.of(modifiers[0], modifiers);
        }
    }

    private static final String HOME = System.getProperty("user.home");

    private final TextGraphics graphics;

    Painter(TextGraphics graphics) {
        this.graphics = graphics;
    }

    int columns() {
        return graphics.getSize().getColumns();
    }

    int rows() {
        return graphics.getSize().getRows();
    }

    /** Writes the text from a column, cut to {@code width}; says where it ended. */
    int text(int column, int row, String text, int width, Style style) {
        String fitted = fit(text, width);
        graphics.setForegroundColor(style.foreground);
        graphics.setBackgroundColor(style.background);
        graphics.setModifiers(style.modifiers);
        graphics.putString(column, row, fitted);
        graphics.clearModifiers();
        graphics.setForegroundColor(TextColor.ANSI.DEFAULT);
        graphics.setBackgroundColor(TextColor.ANSI.DEFAULT);
        return column + fitted.length();
    }

    /** A label and its value on a line: {@code Port         7420}. */
    void field(int column, int row, int width, String label, String value, Style style) {
        int labelWidth = 14;
        text(column, row, label, Math.min(labelWidth, width), Style.DIM);
        text(column + labelWidth, row, value, width - labelWidth, style);
    }

    /** A line of the width, for the selected row. */
    void band(int column, int row, int width, Style style) {
        text(column, row, " ".repeat(Math.max(0, width)), width, style);
    }

    /** A frame with its title in the top border; the inside is from {@code column + 2, row + 1}. */
    void pane(int column, int row, int width, int height, String title, boolean active) {
        if (width < 4 || height < 2) {
            return;
        }
        Style border = active ? Style.GOOD : Style.DIM;
        text(column, row, "┌" + "─".repeat(width - 2) + "┐", width, border);
        for (int line = row + 1; line < row + height - 1; line++) {
            text(column, line, "│", 1, border);
            text(column + width - 1, line, "│", 1, border);
        }
        text(column, row + height - 1, "└" + "─".repeat(width - 2) + "┘", width, border);
        text(column + 2, row, " " + title + " ", width - 4, active ? Style.KEY : Style.TITLE);
    }

    /**
     * A QR code, two rows of modules a line with half blocks, black on white whatever the
     * terminal's colors, with its margin. Says how many lines it took.
     */
    int qr(int column, int row, QrCode qr) {
        int margin = 2;
        int lines = 0;
        for (int y = -margin; y < qr.size + margin; y += 2) {
            for (int x = -margin; x < qr.size + margin; x++) {
                graphics.setForegroundColor(qr.getModule(x, y) ? TextColor.ANSI.BLACK : TextColor.ANSI.WHITE_BRIGHT);
                graphics.setBackgroundColor(qr.getModule(x, y + 1) ? TextColor.ANSI.BLACK : TextColor.ANSI.WHITE_BRIGHT);
                graphics.setCharacter(column + x + margin, row + lines, '▀');
            }
            lines++;
        }
        graphics.setForegroundColor(TextColor.ANSI.DEFAULT);
        graphics.setBackgroundColor(TextColor.ANSI.DEFAULT);
        return lines;
    }

    /** The width of a QR code drawn by {@link #qr}, its margin included. */
    static int qrWidth(QrCode qr) {
        return qr.size + 4;
    }

    /** The text cut to the width, with an ellipsis when something is left out. */
    static String fit(String text, int width) {
        if (width <= 0) {
            return "";
        }
        if (text.length() <= width) {
            return text;
        }
        return width == 1 ? "…" : text.substring(0, width - 1) + "…";
    }

    /** The text padded with spaces, or cut, to the width exactly. */
    static String column(String text, int width) {
        String fitted = fit(text, width);
        return fitted + " ".repeat(Math.max(0, width - fitted.length()));
    }

    /** A folder as one types it, {@code ~/courses/OC}. */
    static String folder(Path folder) {
        String path = folder.toString();
        return path.startsWith(HOME + "/") ? "~" + path.substring(HOME.length()) : path;
    }
}
