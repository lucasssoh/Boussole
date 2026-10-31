package io.github.lucasssoh.boussole.cli.admin;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import io.github.lucasssoh.boussole.adapters.config.BoussoleConfigFile;
import io.github.lucasssoh.boussole.cli.TrackCheck;
import io.github.lucasssoh.boussole.cli.admin.Painter.Style;
import io.github.lucasssoh.boussole.cli.serve.ServerControl;
import io.github.lucasssoh.boussole.format.InspectionReport;
import io.github.lucasssoh.boussole.format.diagnostic.Diagnostic;
import io.github.lucasssoh.boussole.format.model.Course;
import io.github.lucasssoh.boussole.format.source.FolderSourceDocuments;
import io.github.lucasssoh.boussole.format.source.SourceInventory;
import io.github.lucasssoh.boussole.format.source.SourceInventory.SourceState;
import io.github.lucasssoh.boussole.format.source.SourceInventory.State;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * F3: the tracks of {@code config.yaml}, each validated as {@code boussole validate} does, with its
 * documents to take into account; add a folder to the list or take one out.
 */
final class TracksTab implements AdminTab {

    private static final String HOME = System.getProperty("user.home");
    /** Problems a page turns: two lines each. */
    private static final int PAGE = 5;

    private final AdminApp app;
    private final BoussoleConfigFile config;
    private final ServerControl control;
    private volatile List<Track> tracks;
    private volatile boolean reading;
    private int selected;
    private int scroll;

    TracksTab(AdminApp app, BoussoleConfigFile config, ServerControl control) {
        this.app = app;
        this.config = config;
        this.control = control;
    }

    /** A track as read: {@code problem} when its folder cannot be read at all. */
    record Track(Path folder, Optional<Course> course, List<Diagnostic> diagnostics, List<SourceState> documents, String problem) {

        String name() {
            return course.map(read -> read.subject().name()).orElse(String.valueOf(folder.getFileName()));
        }

        long errors() {
            return diagnostics.stream().filter(Diagnostic::isError).count();
        }

        long warnings() {
            return diagnostics.size() - errors();
        }

        String sessions() {
            return course.map(read -> read.sessions().size() + "/"
                    + read.syllabus().chapters().stream().mapToLong(chapter -> chapter.sessions().size()).sum()).orElse("—");
        }

        String documentsLine() {
            return course.isEmpty() ? "—" : documents.isEmpty() ? "up to date" : documents.size() + " to do";
        }
    }

    @Override
    public String name() {
        return "Tracks";
    }

    @Override
    public void refresh() {
        if (tracks == null && !reading) {
            check(null);
        }
    }

    /** Reads every track again, away from the screen. */
    private void check(String done) {
        reading = true;
        app.job("Validating…", () -> {
            try {
                tracks = read(config.read().subjects());
                return done;
            } finally {
                reading = false;
            }
        });
    }

    static List<Track> read(List<Path> subjects) {
        List<Track> read = new ArrayList<>();
        for (Path folder : subjects) {
            if (!Files.isDirectory(folder)) {
                read.add(new Track(folder, Optional.empty(), List.of(), List.of(), "folder not found"));
                continue;
            }
            InspectionReport report = TrackCheck.of(folder, Optional.empty(), subjects);
            List<SourceState> documents = report.course()
                    .map(course -> SourceInventory.compare(course.sources(),
                                    new FolderSourceDocuments(folder.resolve(course.subject().sourcesDir()), folder))
                            .stream()
                            .filter(state -> state.state().needsWork() || state.state() == State.GONE)
                            .toList())
                    .orElse(List.of());
            read.add(new Track(folder, report.course(), report.diagnostics(), documents,
                    report.course().isEmpty() ? "no readable subject.yaml" : ""));
        }
        return List.copyOf(read);
    }

    private Optional<Track> chosen() {
        List<Track> read = tracks;
        if (read == null || read.isEmpty()) {
            return Optional.empty();
        }
        selected = Math.max(0, Math.min(selected, read.size() - 1));
        return Optional.of(read.get(selected));
    }

    @Override
    public void draw(Painter painter, int top, int height) {
        int columns = painter.columns();
        int width = columns - 4;
        List<Track> read = tracks;
        if (read == null) {
            painter.pane(0, top, columns, height, "Tracks", false);
            painter.text(2, top + 1, "Validating…", width, Style.DIM);
            return;
        }
        Optional<Track> current = chosen();
        int listHeight = Math.min(Math.max(1, read.size()) + 3, height / 2);
        painter.pane(0, top, columns, listHeight, "Tracks", false);
        int name = Math.max(10, Math.min(30, read.stream().mapToInt(track -> track.name().length()).max().orElse(0)));
        int numbers = 9 + 2 + 6 + 2 + 8 + 2 + 12;
        int folder = Math.max(10, width - 2 - name - 2 - 2 - numbers);
        painter.text(2, top + 1, "  " + Painter.column("Name", name) + "  " + Painter.column("Folder", folder) + "  "
                + Painter.column("Sessions", 9) + "  " + Painter.column("Errors", 6) + "  " + Painter.column("Warnings", 8)
                + "  Documents", width, Style.DIM);
        if (read.isEmpty()) {
            painter.text(4, top + 2, "none", width - 2, Style.DIM);
        }
        int room = listHeight - 3;
        int first = Math.max(0, selected - room + 1);
        int row = top + 2;
        for (int index = first; index < read.size() && row < top + 2 + room; index++) {
            Track track = read.get(index);
            boolean chosen = index == selected;
            String line = (chosen ? "▸ " : "  ") + Painter.column(track.name(), name) + "  " + Painter.column(Painter.folder(track.folder()), folder)
                    + "  " + Painter.column(track.sessions(), 9) + "  " + Painter.column(String.valueOf(track.errors()), 6) + "  "
                    + Painter.column(String.valueOf(track.warnings()), 8) + "  " + Painter.column(track.documentsLine(), 12);
            Style style = chosen ? Style.SELECTED : !track.problem().isEmpty() || track.errors() > 0 ? Style.ERROR
                    : track.warnings() > 0 || !track.documents().isEmpty() ? Style.WARNING : Style.PLAIN;
            painter.text(2, row++, line, width, style);
        }
        if (current.isEmpty()) {
            return;
        }
        Track track = current.get();
        int detailTop = top + listHeight;
        int detailHeight = height - listHeight;
        int half = columns * 3 / 5;
        drawProblems(painter, track, scroll, 0, detailTop, half, detailHeight);
        drawDocuments(painter, track, half, detailTop, columns - half, detailHeight);
    }

    private static void drawProblems(Painter painter, Track track, int scroll, int column, int top, int width, int height) {
        painter.pane(column, top, width, height, track.name() + " · Problems", false);
        int inner = width - 4;
        int row = top + 1;
        if (!track.problem().isEmpty()) {
            painter.text(column + 2, row, track.problem(), inner, Style.ERROR);
            return;
        }
        if (track.diagnostics().isEmpty()) {
            painter.text(column + 2, row, "none", inner, Style.DIM);
        }
        if (scroll > 0) {
            painter.text(column + width - 4, top, " ↑ ", 3, Style.DIM);
        }
        for (Diagnostic diagnostic : track.diagnostics().subList(Math.min(scroll, track.diagnostics().size()), track.diagnostics().size())) {
            if (row >= top + height - 2) {
                painter.text(column + 2, row, "…", inner, Style.DIM);
                break;
            }
            String place = diagnostic.position().file() + (diagnostic.position().line() > 0 ? ":" + diagnostic.position().line() : "");
            int end = painter.text(column + 2, row, diagnostic.code() + "  ", inner, diagnostic.isError() ? Style.ERROR : Style.WARNING);
            painter.text(end, row++, place, inner - (end - column - 2), Style.PLAIN);
            painter.text(column + 4, row++, diagnostic.message(), inner - 2, Style.DIM);
        }
    }

    private static void drawDocuments(Painter painter, Track track, int column, int top, int width, int height) {
        painter.pane(column, top, width, height, "Documents", false);
        int inner = width - 4;
        int row = top + 1;
        if (track.documents().isEmpty()) {
            painter.text(column + 2, row, track.course().isEmpty() ? "—" : "up to date", inner, Style.DIM);
        }
        for (SourceState document : track.documents()) {
            if (row >= top + height - 1) {
                break;
            }
            int end = painter.text(column + 2, row, Painter.column(document.state().name().toLowerCase(Locale.ROOT), 9), inner, Style.WARNING);
            painter.text(end, row++, document.file(), inner - (end - column - 2), Style.PLAIN);
        }
    }

    @Override
    public boolean key(KeyStroke key) {
        List<Track> read = tracks;
        switch (key.getKeyType()) {
            case ArrowUp -> {
                selected = Math.max(0, selected - 1);
                scroll = 0;
            }
            case ArrowDown -> {
                selected = read == null ? 0 : Math.min(Math.max(0, read.size() - 1), selected + 1);
                scroll = 0;
            }
            case PageDown -> scroll = Math.min(scroll + PAGE, Math.max(0, chosen().map(track -> track.diagnostics().size() - 1).orElse(0)));
            case PageUp -> scroll = Math.max(0, scroll - PAGE);
            case Character -> {
                return character(key.getCharacter());
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    private boolean character(char typed) {
        Optional<Track> track = chosen();
        switch (typed) {
            case 'v' -> {
                if (!reading) {
                    check("Validated.");
                }
            }
            case 'o' -> track.flatMap(Track::course).ifPresent(course -> app.job("Opening…", () -> {
                control.start(() -> { });
                String path = "subjects/" + course.subject().id();
                return (control.open(path) ? "Opened " : "Open ") + control.url(path);
            }));
            case 'f' -> track.ifPresent(found -> {
                if (ServerControl.openFolder(found.folder())) {
                    app.say("Opened " + Painter.folder(found.folder()));
                } else {
                    app.error("No file manager to open " + Painter.folder(found.folder()));
                }
            });
            case '+' -> app.ask("Folder:", "~/", this::add);
            case '-' -> track.ifPresent(found -> app.confirm("Remove " + Painter.folder(found.folder()) + " from the list?", () -> {
                config.removeSubject(found.folder());
                check("Removed " + Painter.folder(found.folder()));
            }));
            default -> {
                return false;
            }
        }
        return true;
    }

    private void add(String typed) {
        if (typed.isEmpty() || typed.equals("~/")) {
            return;
        }
        Path folder = (typed.equals("~") || typed.startsWith("~/") ? Path.of(HOME).resolve(typed.substring(Math.min(2, typed.length())))
                : Path.of(typed)).toAbsolutePath().normalize();
        if (!Files.isRegularFile(folder.resolve("subject.yaml"))) {
            app.error(Painter.folder(folder) + ": no subject.yaml. boussole init " + Painter.folder(folder) + " creates one.");
        } else if (config.addSubject(folder)) {
            check("Added " + Painter.folder(folder));
        } else {
            app.say("Already listed: " + Painter.folder(folder));
        }
    }

    @Override
    public List<Hint> hints() {
        return List.of(new Hint("v", "Validate"), new Hint("PgDn", "Problems"), new Hint("o", "Open"), new Hint("f", "Folder"), new Hint("+", "Add"), new Hint("-", "Remove"));
    }
}
