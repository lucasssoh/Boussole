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
import io.github.lucasssoh.boussole.format.model.CourseSyllabus;
import io.github.lucasssoh.boussole.format.model.SyllabusChapter;
import io.github.lucasssoh.boussole.format.model.SyllabusSession;
import io.github.lucasssoh.boussole.format.model.SyllabusStatus;
import io.github.lucasssoh.boussole.format.source.FolderSourceDocuments;
import io.github.lucasssoh.boussole.format.source.SourceInventory;
import io.github.lucasssoh.boussole.format.source.SourceInventory.SourceState;
import io.github.lucasssoh.boussole.format.source.SourceInventory.State;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import java.util.Locale;
import java.util.Optional;

/**
 * F3: the tracks of {@code config.yaml}, each validated as {@code boussole validate} does, with its
 * documents to take into account; its plan, to approve when an agent proposed it, and the agent
 * that writes its sessions, with the tokens it spends; add a folder to the list or take one out.
 * The folders next to the listed tracks that hold a plan are shown too, to add.
 */
final class TracksTab implements AdminTab {

    private static final String HOME = System.getProperty("user.home");
    /** Problems a page turns: two lines each. */
    private static final int PAGE = 5;
    /** The width the plan is counted at to scroll it: the narrowest screen. */
    private static final int PLAN_WIDTH = 76;

    private final AdminApp app;
    private final BoussoleConfigFile config;
    private final ServerControl control;
    private volatile List<Track> tracks;
    private volatile boolean reading;
    private int selected;
    private int scroll;
    private boolean plan;
    /** The last agent run of each track, read again while the screen shows. */
    private final Map<Path, AgentRun.Status> agents = new ConcurrentHashMap<>();

    TracksTab(AdminApp app, BoussoleConfigFile config, ServerControl control) {
        this.app = app;
        this.config = config;
        this.control = control;
    }

    /** A track as read: {@code problem} when its folder cannot be read at all. */
    record Track(Path folder, Optional<Course> course, List<Diagnostic> diagnostics, List<SourceState> documents, String problem,
                 boolean listed) {

        Track(Path folder, Optional<Course> course, List<Diagnostic> diagnostics, List<SourceState> documents, String problem) {
            this(folder, course, diagnostics, documents, problem, true);
        }

        Track unlisted() {
            return new Track(folder, course, diagnostics, documents, problem, false);
        }

        long left() {
            return course.map(read -> read.syllabus().chapters().stream().mapToLong(chapter -> chapter.sessions().size()).sum()
                    - read.sessions().size()).orElse(0L);
        }

        boolean approved() {
            return course.map(read -> read.syllabus().status() == SyllabusStatus.APPROVED && !read.syllabus().chapters().isEmpty())
                    .orElse(false);
        }

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

        String planLine() {
            return course.map(read -> read.syllabus().chapters().isEmpty() ? "none"
                    : read.syllabus().status().name().toLowerCase(Locale.ROOT)).orElse("—");
        }

        boolean proposed() {
            return course.map(read -> read.syllabus().status() == SyllabusStatus.PROPOSED && !read.syllabus().chapters().isEmpty())
                    .orElse(false);
        }

        boolean written(SyllabusChapter chapter, SyllabusSession session) {
            return course.map(read -> read.sessions().stream()
                    .anyMatch(found -> found.chapter().id().equals(chapter.id()) && found.entry().id().equals(session.id())))
                    .orElse(false);
        }

        String problemsLine() {
            return course.isEmpty() ? "—" : errors() + warnings() == 0 ? "none" : errors() + " E · " + warnings() + " W";
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
        List<Track> read = tracks;
        if (read != null) {
            for (Track track : read) {
                AgentRun.last(track.folder()).ifPresentOrElse(status -> agents.put(track.folder(), status),
                        () -> agents.remove(track.folder()));
            }
        }
    }

    private Optional<AgentRun.Status> agent(Track track) {
        return Optional.ofNullable(agents.get(track.folder()));
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
        List<Track> read = new ArrayList<>(read(subjects, subjects));
        neighbours(subjects).stream()
                .map(folder -> read(List.of(folder), subjects).getFirst())
                .filter(track -> track.course().map(course -> !course.syllabus().chapters().isEmpty()).orElse(false))
                .map(Track::unlisted)
                .forEach(read::add);
        return List.copyOf(read);
    }

    /** The folders beside the listed tracks, one level, that hold a {@code subject.yaml} and are not listed. */
    static List<Path> neighbours(List<Path> subjects) {
        Set<Path> listed = Set.copyOf(subjects.stream().map(folder -> folder.toAbsolutePath().normalize()).toList());
        Set<Path> found = new LinkedHashSet<>();
        for (Path parent : subjects.stream().map(folder -> folder.toAbsolutePath().normalize().getParent()).distinct().toList()) {
            if (parent == null || !Files.isDirectory(parent)) {
                continue;
            }
            try (Stream<Path> children = Files.list(parent)) {
                children.filter(child -> Files.isRegularFile(child.resolve("subject.yaml")))
                        .map(child -> child.toAbsolutePath().normalize())
                        .filter(child -> !listed.contains(child))
                        .sorted()
                        .forEach(found::add);
            } catch (IOException e) {
                // a folder that cannot be listed hides nothing the learner listed
            }
        }
        return List.copyOf(found);
    }

    private static List<Track> read(List<Path> folders, List<Path> subjects) {
        List<Track> read = new ArrayList<>();
        for (Path folder : folders) {
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
        int numbers = 9 + 2 + 8 + 2 + 13 + 2 + 10 + 2 + 12;
        int folder = Math.max(10, width - 2 - name - 2 - 2 - numbers);
        painter.text(2, top + 1, "  " + Painter.column("Name", name) + "  " + Painter.column("Folder", folder) + "  "
                + Painter.column("Sessions", 9) + "  " + Painter.column("Plan", 8) + "  " + Painter.column("Agent", 13) + "  "
                + Painter.column("Problems", 10)
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
                    + "  " + Painter.column(track.sessions(), 9) + "  " + Painter.column(track.planLine(), 8) + "  "
                    + Painter.column(agent(track).map(AgentRun.Status::line).orElse("—"), 13) + "  "
                    + Painter.column(track.problemsLine(), 10) + "  "
                    + Painter.column(track.listed() ? track.documentsLine() : "not listed", 12);
            Style style = chosen ? Style.SELECTED : !track.listed() ? Style.DIM : !track.problem().isEmpty() || track.errors() > 0 ? Style.ERROR
                    : track.warnings() > 0 || !track.documents().isEmpty() || track.proposed() ? Style.WARNING : Style.PLAIN;
            painter.text(2, row++, line, width, style);
        }
        if (current.isEmpty()) {
            return;
        }
        Track track = current.get();
        int detailTop = top + listHeight;
        int detailHeight = height - listHeight;
        if (plan) {
            drawPlan(painter, track, agent(track), scroll, detailTop, columns, detailHeight);
            return;
        }
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

    /** One line of the plan as drawn. */
    private record PlanLine(String text, Style style) {
    }

    /** Under the title of its session, where a goal starts. */
    private static final String GOAL = " ".repeat(19);

    static List<String> planText(Track track, Optional<AgentRun.Status> agent, int width) {
        return planLines(track, agent, width).stream().map(PlanLine::text).toList();
    }

    private static List<PlanLine> planLines(Track track, Optional<AgentRun.Status> agent, int width) {
        List<PlanLine> lines = new ArrayList<>();
        if (track.course().isEmpty()) {
            return lines;
        }
        agent.ifPresent(status -> agentLines(status, width, lines));
        CourseSyllabus syllabus = track.course().get().syllabus();
        if (syllabus.chapters().isEmpty()) {
            lines.add(new PlanLine("No plan yet: an agent proposes one with the cursus-plan playbook.", Style.DIM));
            return lines;
        }
        List<SyllabusSession> all = syllabus.chapters().stream().flatMap(chapter -> chapter.sessions().stream()).toList();
        int minutes = all.stream().mapToInt(SyllabusSession::duration).sum();
        lines.add(syllabus.status() == SyllabusStatus.PROPOSED
                ? new PlanLine("Proposed by an agent: read it, then a approves it and the agents write its sessions.", Style.WARNING)
                : new PlanLine(track.left() == 0 ? "Approved, and every session is written."
                        : "Approved: r starts an agent to write the " + count((int) track.left(), "session") + " left, in this order.",
                        Style.GOOD));
        lines.add(new PlanLine(count(syllabus.chapters().size(), "chapter") + " · " + count(all.size(), "session") + " · " + duration(minutes),
                Style.DIM));
        int number = 0;
        for (SyllabusChapter chapter : syllabus.chapters()) {
            lines.add(new PlanLine("", Style.PLAIN));
            lines.add(new PlanLine(++number + ". " + chapter.title(), Style.TITLE));
            for (SyllabusSession session : chapter.sessions()) {
                boolean written = track.written(chapter, session);
                lines.add(new PlanLine("   " + (written ? "✓ " : "  ") + Painter.column(session.id(), 5) + " "
                        + Painter.column(session.duration() + " min", 7) + " " + session.title(), written ? Style.GOOD : Style.PLAIN));
                if (session.goal() != null && !session.goal().isBlank()) {
                    wrap(session.goal(), width - GOAL.length()).forEach(line -> lines.add(new PlanLine(GOAL + line, Style.DIM)));
                }
            }
        }
        return lines;
    }

    private static void agentLines(AgentRun.Status status, int width, List<PlanLine> lines) {
        AgentRun.Tokens tokens = status.tokens();
        Style style = switch (status.state()) {
            case RUNNING -> Style.WARNING;
            case DONE -> Style.GOOD;
            case FAILED -> Style.ERROR;
            case STOPPED -> Style.DIM;
        };
        String state = switch (status.state()) {
            case RUNNING -> "Agent running" + (status.started().getEpochSecond() > 0 ? " since "
                    + LocalTime.ofInstant(status.started(), ZoneId.systemDefault()).withNano(0) : "")
                    + ": x stops it.";
            case DONE -> "Agent done" + status.took().map(took -> " in " + duration((int) took.toMinutes())).orElse("") + ".";
            case FAILED -> "Agent failed" + (status.result().isBlank() ? "." : ": " + oneLine(status.result()));
            case STOPPED -> "Agent stopped before the end.";
        };
        lines.add(new PlanLine(state, style));
        lines.add(new PlanLine("Tokens " + AgentRun.count(tokens.total()) + ": " + AgentRun.count(tokens.input()) + " in, "
                + AgentRun.count(tokens.output()) + " out, " + AgentRun.count(tokens.cacheRead()) + " read from the cache, "
                + AgentRun.count(tokens.cacheWrite()) + " written to it"
                + status.costUsd().map(cost -> String.format(Locale.ROOT, " · $%.2f at list price", cost)).orElse(""), Style.PLAIN));
        List<String> recent = status.state() == AgentRun.State.RUNNING ? status.recent()
                : status.result().isBlank() ? List.of() : List.of(oneLine(status.result()));
        recent.forEach(line -> lines.add(new PlanLine("  " + Painter.fit(line, Math.max(10, width - 2)), Style.DIM)));
        lines.add(new PlanLine("Log: " + Painter.folder(status.log()), Style.DIM));
        lines.add(new PlanLine("", Style.PLAIN));
    }

    private static String oneLine(String text) {
        return text.strip().replaceAll("\\s+", " ");
    }

    static List<String> wrap(String text, int width) {
        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : text.trim().split("\\s+")) {
            if (!line.isEmpty() && line.length() + 1 + word.length() > Math.max(20, width)) {
                lines.add(line.toString());
                line.setLength(0);
            }
            line.append(line.isEmpty() ? "" : " ").append(word);
        }
        if (!line.isEmpty()) {
            lines.add(line.toString());
        }
        return lines;
    }

    private static String count(int number, String word) {
        return number + " " + word + (number == 1 ? "" : "s");
    }

    private static String duration(int minutes) {
        return minutes < 60 ? minutes + " min" : minutes / 60 + " h" + (minutes % 60 == 0 ? "" : String.format(Locale.ROOT, " %02d", minutes % 60));
    }

    private static void drawPlan(Painter painter, Track track, Optional<AgentRun.Status> agent, int scroll, int top, int width,
                                 int height) {
        painter.pane(0, top, width, height, track.name() + " · Plan", false);
        int inner = width - 4;
        List<PlanLine> lines = planLines(track, agent, inner);
        int room = height - 2;
        int first = Math.max(0, Math.min(scroll, lines.size() - room));
        if (first > 0) {
            painter.text(width - 4, top, " ↑ ", 3, Style.DIM);
        }
        int row = top + 1;
        for (int index = first; index < lines.size() && row < top + 1 + room; index++) {
            painter.text(2, row++, lines.get(index).text(), inner, lines.get(index).style());
        }
        if (first + room < lines.size()) {
            painter.text(width - 4, top + height - 1, " ↓ ", 3, Style.DIM);
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
            case PageDown -> scroll = Math.min(scroll + (plan ? PAGE * 2 : PAGE), Math.max(0,
                    chosen().map(track -> (plan ? planLines(track, agent(track), PLAN_WIDTH).size() : track.diagnostics().size()) - 1)
                            .orElse(0)));
            case PageUp -> scroll = Math.max(0, scroll - (plan ? PAGE * 2 : PAGE));
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
            case 'p' -> {
                plan = !plan;
                scroll = 0;
            }
            case 'a' -> track.ifPresent(this::approve);
            case 'r' -> track.ifPresent(this::run);
            case 'x' -> track.ifPresent(this::stop);
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
            case '+' -> {
                if (track.isPresent() && !track.get().listed()) {
                    add(track.get().folder().toString());
                } else {
                    app.ask("Folder:", "~/", this::add);
                }
            }
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

    private void approve(Track track) {
        if (!track.proposed()) {
            app.say(track.course().isEmpty() ? "No plan to approve." : "The plan of " + track.name() + " is " + track.planLine() + ".");
            return;
        }
        plan = true;
        app.confirm("Approve the plan of " + track.name() + "? The agents then write its sessions.", () -> {
            PlanApproval.approve(track.folder());
            check("Plan of " + track.name() + " approved.");
        });
    }

    private void run(Track track) {
        if (agent(track).map(status -> status.state() == AgentRun.State.RUNNING).orElse(false)) {
            app.say("An agent already writes " + track.name() + ": x stops it.");
        } else if (!track.approved()) {
            app.say(track.course().isEmpty() ? "No plan here." : "Approve the plan first: p shows it, a approves it.");
        } else if (track.left() == 0) {
            app.say("Every session of " + track.name() + " is written.");
        } else {
            plan = true;
            app.confirm("Start an agent to write the " + count((int) track.left(), "session") + " left of " + track.name()
                    + "? It runs Claude Code and spends tokens.", () -> app.job("Starting the agent…", () -> {
                Path log = AgentRun.start(track.folder(), AgentRun.command(System.getProperty("boussole.agent", "claude"),
                        System.getProperty("boussole.jar")));
                agents.put(track.folder(), AgentRun.read(log));
                return "Agent started on " + track.name() + ".";
            }));
        }
    }

    private void stop(Track track) {
        Optional<AgentRun.Status> running = agent(track).filter(status -> status.state() == AgentRun.State.RUNNING);
        if (running.isEmpty()) {
            app.say("No agent runs on " + track.name() + ".");
            return;
        }
        app.confirm("Stop the agent of " + track.name() + "? What it wrote stays.", () -> {
            AgentRun.stop(running.get().log());
            app.say("Agent of " + track.name() + " stopped.");
        });
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
        Optional<Track> chosen = chosen();
        boolean proposed = chosen.map(Track::proposed).orElse(false);
        boolean running = chosen.flatMap(this::agent).map(status -> status.state() == AgentRun.State.RUNNING).orElse(false);
        List<Hint> hints = new ArrayList<>(List.of(new Hint("v", "Validate"), new Hint("p", plan ? "Problems" : "Plan"),
                new Hint("PgDn", plan ? "Scroll" : "Problems")));
        if (proposed) {
            hints.add(new Hint("a", "Approve"));
        }
        if (running) {
            hints.add(new Hint("x", "Stop agent"));
        } else if (chosen.map(track -> track.approved() && track.left() > 0).orElse(false)) {
            hints.add(new Hint("r", "Run agent"));
        }
        hints.addAll(List.of(new Hint("o", "Open"), new Hint("f", "Folder"), new Hint("+", "Add"), new Hint("-", "Remove")));
        return hints;
    }
}
