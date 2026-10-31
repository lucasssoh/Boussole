package io.github.lucasssoh.boussole.cli.admin;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import io.github.lucasssoh.boussole.adapters.config.BoussoleConfigFile;
import io.github.lucasssoh.boussole.cli.admin.AdminTab.Hint;
import io.github.lucasssoh.boussole.cli.admin.Painter.Style;
import io.github.lucasssoh.boussole.cli.serve.ServerControl;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * The full screen of {@code boussole} alone: a line of screens, the screen shown, a line for
 * messages and questions, the keys. What talks to the server or reads the tracks runs on a thread
 * of its own, one thing at a time, so that the keys always answer.
 */
final class AdminApp {

    static final int MIN_COLUMNS = 80;
    static final int MIN_ROWS = 24;
    private static final Duration MESSAGE_TIME = Duration.ofSeconds(8);
    private static final List<Hint> GLOBAL = List.of(new Hint("F1–F3", "Screen"), new Hint("q", "Quit"));

    private final Screen screen;
    private final String version;
    private final ServerTab server;
    private final List<AdminTab> tabs;
    private final ExecutorService jobs = Executors.newSingleThreadExecutor(Thread.ofPlatform().daemon().factory());
    private ScheduledExecutorService ticks;

    private volatile int current;
    private volatile boolean quit;
    private volatile Message message;
    private Prompt prompt;

    AdminApp(Screen screen, BoussoleConfigFile config, ServerControl control, String version) {
        this.screen = screen;
        this.version = version;
        this.server = new ServerTab(this, config, control);
        this.tabs = List.of(server, new DevicesTab(this, config), new TracksTab(this, config, control));
    }

    void run() throws IOException, InterruptedException {
        screen.startScreen();
        ticks = Executors.newSingleThreadScheduledExecutor(Thread.ofPlatform().daemon().factory());
        ticks.scheduleWithFixedDelay(this::refresh, 0, 1, TimeUnit.SECONDS);
        try {
            while (!quit) {
                KeyStroke key = screen.pollInput();
                if (key == null) {
                    Thread.sleep(50);
                } else {
                    key(key);
                }
                draw();
            }
        } finally {
            ticks.shutdownNow();
            jobs.shutdownNow();
            screen.stopScreen();
        }
    }

    /** The server state for the top line, and the screen shown. */
    void refresh() {
        try {
            server.refresh();
            AdminTab tab = tabs.get(current);
            if (tab != server) {
                tab.refresh();
            }
        } catch (RuntimeException e) {
            error(Objects.requireNonNullElse(e.getMessage(), e.toString()));
        }
    }

    void key(KeyStroke key) {
        if (prompt != null) {
            Prompt open = prompt;
            prompt = null;
            Prompt next = open.key(key);
            if (prompt == null) {
                prompt = next;
            }
            return;
        }
        if (key.getKeyType() == KeyType.EOF || key.getKeyType() == KeyType.Character && key.isCtrlDown() && key.getCharacter() == 'c') {
            quit = true;
            return;
        }
        if (message != null && message.until != null) {
            message = null;
        }
        if (tabs.get(current).key(key)) {
            return;
        }
        int screenKey = switch (key.getKeyType()) {
            case F1 -> 0;
            case F2 -> 1;
            case F3 -> 2;
            case Character -> "123".indexOf(key.getCharacter());
            default -> -1;
        };
        if (screenKey >= 0 && screenKey < tabs.size()) {
            show(screenKey);
        } else if (key.getKeyType() == KeyType.Character && key.getCharacter() == 'q') {
            quit = true;
        }
    }

    void show(int tab) {
        current = tab;
        if (ticks != null) {
            ticks.execute(this::refresh);
        }
    }

    boolean running() {
        return server.running();
    }

    /** Runs the work away from the screen, saying {@code working} meanwhile, then what it returns. */
    void job(String working, Callable<String> work) {
        if (working != null) {
            message = new Message(working, Style.DIM, null);
        }
        jobs.execute(() -> {
            try {
                String done = work.call();
                if (done != null) {
                    say(done);
                } else if (working != null) {
                    message = null;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                error(Objects.requireNonNullElse(e.getMessage(), e.toString()));
            }
            refresh();
        });
    }

    void say(String text) {
        message = new Message(text, Style.PLAIN, Instant.now().plus(MESSAGE_TIME));
    }

    void error(String text) {
        message = new Message(text, Style.ERROR, Instant.now().plus(MESSAGE_TIME));
    }

    /** Asks for a line of text on the bottom line; Enter gives it to {@code then}, Esc lets it go. */
    void ask(String label, String initial, Consumer<String> then) {
        prompt = new Prompt(label, new StringBuilder(initial), then, false);
    }

    /** Asks a question answered with y; any other key is no. */
    void confirm(String question, Runnable then) {
        prompt = new Prompt(question + " y/n", new StringBuilder(), answer -> then.run(), true);
    }

    void quit() {
        quit = true;
    }

    void draw() throws IOException {
        screen.doResizeIfNecessary();
        // Blanked rather than cleared: Screen.clear() redraws the whole terminal at the next refresh, which flickers.
        screen.newTextGraphics().fill(' ');
        Painter painter = new Painter(screen.newTextGraphics());
        int columns = painter.columns();
        int rows = painter.rows();
        screen.setCursorPosition(null);
        if (columns < MIN_COLUMNS || rows < MIN_ROWS) {
            painter.text(0, 0, "Terminal " + columns + "×" + rows + ", " + MIN_COLUMNS + "×" + MIN_ROWS + " at least.", columns, Style.PLAIN);
            screen.refresh();
            return;
        }
        int end = painter.text(0, 0, "Boussole ", columns, Style.KEY);
        end = painter.text(end, 0, version, columns - end, Style.DIM);
        painter.text(end, 0, "  beta", columns - end, Style.WARNING);
        String state = running() ? "● Running" : "○ Stopped";
        painter.text(columns - state.length(), 0, state, state.length(), running() ? Style.GOOD : Style.DIM);
        int column = 0;
        for (int index = 0; index < tabs.size(); index++) {
            String tab = " F" + (index + 1) + " " + tabs.get(index).name() + " ";
            column = painter.text(column, 1, tab, columns - column, index == current ? Style.TAB : Style.DIM) + 1;
        }
        painter.text(0, 2, "─".repeat(columns), columns, Style.DIM);
        tabs.get(current).draw(painter, 3, rows - 5);
        drawMessage(painter, rows - 2, columns);
        drawHints(painter, rows - 1, columns);
        screen.refresh();
    }

    private void drawMessage(Painter painter, int row, int columns) {
        if (prompt != null) {
            int end = painter.text(0, row, prompt.label + " ", columns, Style.TITLE);
            end = painter.text(end, row, prompt.text.toString(), columns - end - 1, Style.PLAIN);
            screen.setCursorPosition(new TerminalPosition(end, row));
            return;
        }
        Message shown = message;
        if (shown != null && shown.until != null && Instant.now().isAfter(shown.until)) {
            message = null;
            shown = null;
        }
        if (shown != null) {
            painter.text(0, row, shown.text, columns, shown.style);
        }
    }

    private void drawHints(Painter painter, int row, int columns) {
        int column = 0;
        for (Hint hint : prompt != null ? prompt.hints() : concat(tabs.get(current).hints(), GLOBAL)) {
            column = painter.text(column, row, hint.key(), columns - column, Style.KEY);
            column = painter.text(column, row, " " + hint.label() + "   ", columns - column, Style.DIM);
        }
    }

    private static List<Hint> concat(List<Hint> first, List<Hint> second) {
        return java.util.stream.Stream.concat(first.stream(), second.stream()).toList();
    }

    private record Message(String text, Style style, Instant until) {
    }

    /** The question of the bottom line, and what is typed. */
    private record Prompt(String label, StringBuilder text, Consumer<String> then, boolean yesNo) {

        /** The prompt still open after the key, or null. */
        Prompt key(KeyStroke key) {
            if (yesNo) {
                if (key.getKeyType() == KeyType.Character && Character.toLowerCase(key.getCharacter()) == 'y') {
                    then.accept("y");
                }
                return null;
            }
            switch (key.getKeyType()) {
                case Enter -> {
                    then.accept(text.toString().strip());
                    return null;
                }
                case Escape -> {
                    return null;
                }
                case Backspace -> {
                    if (!text.isEmpty()) {
                        text.deleteCharAt(text.length() - 1);
                    }
                }
                case Character -> {
                    if (!key.isCtrlDown()) {
                        text.append(key.getCharacter());
                    }
                }
                default -> {
                }
            }
            return this;
        }

        List<Hint> hints() {
            return yesNo ? List.of(new Hint("y", "Yes"), new Hint("n", "No")) : List.of(new Hint("Enter", "OK"), new Hint("Esc", "Cancel"));
        }
    }
}
