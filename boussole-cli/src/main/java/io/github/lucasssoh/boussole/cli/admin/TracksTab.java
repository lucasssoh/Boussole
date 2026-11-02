package io.github.lucasssoh.boussole.cli.admin;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.input.MouseAction;
import io.github.lucasssoh.boussole.adapters.config.BoussoleConfigFile;
import io.github.lucasssoh.boussole.cli.TrackBrief;
import io.github.lucasssoh.boussole.cli.TrackCheck;
import io.github.lucasssoh.boussole.cli.TrackLock;
import io.github.lucasssoh.boussole.cli.admin.Painter.Style;
import io.github.lucasssoh.boussole.cli.serve.ServerControl;
import io.github.lucasssoh.boussole.format.InspectionReport;
import io.github.lucasssoh.boussole.format.diagnostic.Diagnostic;
import io.github.lucasssoh.boussole.format.model.ChapterLinks;
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
import java.time.Duration;
import java.time.Instant;
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
    /** Lines, or problems, a turn of the wheel scrolls. */
    private static final int WHEEL = 3;
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
    /** The documents being ticked before {@code r} takes them in, or null. */
    private DocumentPick pick;
    /** While an agent writes, the tracks are validated again this often. */
    private static final Duration RECHECK = Duration.ofSeconds(30);
    private volatile Instant checked = Instant.EPOCH;
    /** The last agent run of each track, read again while the screen shows. */
    private final Map<Path, AgentRun.Status> agents = new ConcurrentHashMap<>();
    /** The sessions of agents on the bus of each track, and the session files changed a moment ago. */
    private final Map<Path, List<AgentBus.Session>> buses = new ConcurrentHashMap<>();
    private final Map<Path, Map<String, String>> touched = new ConcurrentHashMap<>();
    /** A session file changed this recently is taken as being written, whatever writes it. */
    private static final Duration TOUCHED = Duration.ofMinutes(2);

    /**
     * What works on a track: the agent {@code r} started, the sessions on its bus (any agent), and
     * the session files changed a moment ago, chapter folder to file, for an agent that says nothing.
     */
    record Watch(Optional<AgentRun.Status> run, List<AgentBus.Session> bus, Map<String, String> touched) {

        static final Watch NONE = new Watch(Optional.empty(), List.of(), Map.of());

        boolean runRunning() {
            return run.map(status -> status.state() == AgentRun.State.RUNNING).orElse(false);
        }

        /** The bus sessions to show apart: not the one of the run, already shown with it. */
        List<AgentBus.Session> others() {
            String own = run.map(AgentRun.Status::sessionId).orElse("");
            return bus.stream().filter(session -> !session.id().equals(own)).toList();
        }

        boolean working() {
            return runRunning() || others().stream().anyMatch(session -> session.state() == AgentBus.SessionState.WORKING)
                    || !touched.isEmpty();
        }

        /** The session file being written in a chapter folder, and the last action there. */
        Optional<AgentRun.Chapter> chapter(String dir) {
            Optional<AgentRun.Chapter> found = run.filter(status -> status.state() == AgentRun.State.RUNNING)
                    .flatMap(status -> status.chapter(dir));
            for (AgentBus.Session session : others()) {
                if (found.isEmpty() && session.state() != AgentBus.SessionState.ENDED) {
                    found = Optional.ofNullable(session.chapters().get(dir));
                }
            }
            return found.or(() -> Optional.ofNullable(touched.get(dir)).map(file -> new AgentRun.Chapter(file, "")));
        }
    }

    private Watch watch(Track track) {
        return new Watch(agent(track), buses.getOrDefault(track.folder(), List.of()), touched.getOrDefault(track.folder(), Map.of()));
    }

    /** The session files of the plan changed less than {@link #TOUCHED} ago: chapter folder to the latest. */
    static Map<String, String> touched(Track track, Instant now) {
        Map<String, String> found = new java.util.HashMap<>();
        track.course().ifPresent(course -> {
            for (SyllabusChapter chapter : course.syllabus().chapters()) {
                Instant latest = Instant.EPOCH;
                for (SyllabusSession session : chapter.sessions()) {
                    try {
                        Path file = track.folder().resolve("cursus").resolve(chapter.dir()).resolve(session.file());
                        Instant changed = Files.isRegularFile(file) ? Files.getLastModifiedTime(file).toInstant() : Instant.EPOCH;
                        if (changed.isAfter(now.minus(TOUCHED)) && changed.isAfter(latest)) {
                            latest = changed;
                            found.put(chapter.dir(), session.file());
                        }
                    } catch (IOException e) {
                        // a file that cannot be read is not being written
                    }
                }
            }
        });
        return Map.copyOf(found);
    }

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

        /** The sessions an agent may write now: the final mock exam waits for the lock. */
        long left() {
            return course.map(read -> read.syllabus().chapters().stream().mapToLong(chapter -> chapter.sessions().size()).sum()
                    - read.sessions().size() - (TrackBrief.finalExamWaiting(read).isPresent() ? 1 : 0)).orElse(0L);
        }

        /** The documents that ask for an agent: new, changed, gone; once locked, only those gone. */
        int toTakeIn() {
            return course.map(read -> TrackBrief.toTakeIn(read, documents).size()).orElse(0);
        }

        boolean locked() {
            return course.map(read -> read.syllabus().isLocked()).orElse(false);
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
            return course.isEmpty() ? "—" : errors() > 0 ? count((int) errors(), "error")
                    : warnings() > 0 ? count((int) warnings(), "warning") : "none";
        }

        String documentsLine() {
            return course.isEmpty() ? "—" : locked() ? toTakeIn() > 0 ? toTakeIn() + " to do" : "locked"
                    : documents.isEmpty() ? "up to date" : documents.size() + " to do";
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
        if (read == null) {
            return;
        }
        boolean writing = false;
        boolean ended = false;
        Instant moment = Instant.now();
        for (Track track : read) {
            boolean before = watch(track).working();
            AgentRun.last(track.folder()).ifPresentOrElse(status -> agents.put(track.folder(), status), () -> agents.remove(track.folder()));
            buses.put(track.folder(), AgentBus.sessions(track.folder(), moment));
            touched.put(track.folder(), touched(track, moment));
            boolean working = watch(track).working();
            writing |= working;
            ended |= before && !working;
        }
        // the sessions an agent writes show up as it goes
        if (!reading && (ended || writing && Duration.between(checked, Instant.now()).compareTo(RECHECK) > 0)) {
            check(null);
        }
    }

    private Optional<AgentRun.Status> agent(Track track) {
        return Optional.ofNullable(agents.get(track.folder()));
    }

    /** Reads every track again, away from the screen. */
    private void check(String done) {
        reading = true;
        checked = Instant.now();
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
        int numbers = 9 + 2 + 8 + 2 + 13 + 2 + 12 + 2 + 12;
        int folder = Math.max(10, width - 2 - name - 2 - 2 - numbers);
        painter.text(2, top + 1, "  " + Painter.column("Name", name) + "  " + Painter.column("Folder", folder) + "  "
                + Painter.column("Sessions", 9) + "  " + Painter.column("Plan", 8) + "  " + Painter.column("Agent", 13) + "  "
                + Painter.column("Problems", 12)
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
                    + Painter.column(agentColumn(watch(track), track), 13) + "  "
                    + Painter.column(track.problemsLine(), 12) + "  "
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
            scroll = drawPlan(painter, track, watch(track), scroll, detailTop, columns, detailHeight);
            return;
        }
        int half = columns * 3 / 5;
        scroll = drawProblems(painter, track, scroll, 0, detailTop, half, detailHeight);
        drawDocuments(painter, track, pick != null && pick.track().equals(track.folder()) ? pick : null, half, detailTop,
                columns - half, detailHeight);
    }

    /**
     * Draws the problems from the {@code scroll}-th, or from the last one that still fills the
     * pane; returns where it started, so that going back up moves at once.
     */
    private static int drawProblems(Painter painter, Track track, int scroll, int column, int top, int width, int height) {
        painter.pane(column, top, width, height, track.name() + " · Problems", false);
        int inner = width - 4;
        int row = top + 1;
        if (!track.problem().isEmpty()) {
            painter.text(column + 2, row, track.problem(), inner, Style.ERROR);
            return 0;
        }
        int fit = Math.max(1, (height - 3) / 2);
        scroll = Math.max(0, Math.min(scroll, track.diagnostics().size() - fit));
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
        return scroll;
    }

    /** One line of the plan as drawn. */
    private record PlanLine(String text, Style style) {
    }

    /** Under the title of its session, where a goal starts. */
    private static final String GOAL = " ".repeat(19);

    static List<String> planText(Track track, Optional<AgentRun.Status> agent, int width) {
        Instant now = Instant.now();
        Watch watch = new Watch(agent, AgentBus.sessions(track.folder(), now), touched(track, now));
        return planLines(track, watch, width).stream().map(PlanLine::text).toList();
    }

    private static List<PlanLine> planLines(Track track, Watch watch, int width) {
        Optional<AgentRun.Status> agent = watch.run();
        List<PlanLine> lines = new ArrayList<>();
        if (track.course().isEmpty()) {
            return lines;
        }
        Course course = track.course().get();
        CourseSyllabus syllabus = course.syllabus();
        if (syllabus.chapters().isEmpty()) {
            lines.add(new PlanLine("No plan yet.", Style.DIM));
            return lines;
        }
        List<SyllabusSession> all = syllabus.chapters().stream().flatMap(chapter -> chapter.sessions().stream()).toList();
        int written = course.sessions().size();
        boolean running = watch.working();
        agent.filter(status -> status.state() == AgentRun.State.RUNNING || watch.others().isEmpty())
                .ifPresent(status -> agentLines(status, lines));
        watch.others().forEach(session -> busLines(session, lines));
        if (syllabus.status() == SyllabusStatus.PROPOSED) {
            lines.add(new PlanLine("Waiting for your approval.", Style.WARNING));
        } else {
            lines.add(new PlanLine((running ? movingBar(written, all.size(), 30) : bar(written, all.size(), 30))
                    + "  " + written + " of " + count(all.size(), "session"),
                    written == all.size() ? Style.GOOD : Style.PLAIN));
        }
        lines.add(new PlanLine(count(syllabus.chapters().size(), "chapter") + " · "
                + (syllabus.status() == SyllabusStatus.PROPOSED ? count(all.size(), "session") + " · " : "")
                + duration(all.stream().mapToInt(SyllabusSession::duration).sum()), Style.DIM));
        ChapterLinks links = ChapterLinks.of(course);
        int titleWidth = Math.max(16, Math.min(40, width - 40));
        int number = 0;
        for (SyllabusChapter chapter : syllabus.chapters()) {
            int done = (int) chapter.sessions().stream().filter(session -> track.written(chapter, session)).count();
            Optional<AgentRun.Chapter> work = watch.chapter(chapter.dir());
            Optional<SyllabusSession> current = work.flatMap(found -> chapter.sessions().stream()
                    .filter(session -> session.file().equals(found.file())).findFirst());
            String state = current.map(session -> "writing " + session.id())
                    .orElse(done == chapter.sessions().size() ? "done" : done > 0 ? "" : waitsFor(chapter, links, syllabus, track));
            lines.add(new PlanLine("", Style.PLAIN));
            lines.add(new PlanLine(Painter.column(++number + ". " + chapter.title(), titleWidth) + "  "
                    + (current.isPresent() ? movingBar(done, chapter.sessions().size(), 10) : bar(done, chapter.sessions().size(), 10)) + "  " + Painter.column(done + " of " + chapter.sessions().size(), 9)
                    + "  " + state, Style.TITLE));
            for (SyllabusSession session : chapter.sessions()) {
                boolean writing = current.filter(session::equals).isPresent();
                boolean isWritten = track.written(chapter, session);
                String mark = writing ? spinner() + " " : isWritten ? "✓ " : "· ";
                String line = "   " + mark + Painter.column(session.id(), 5) + " " + Painter.column(session.duration() + " min", 7) + " "
                        + session.title();
                lines.add(new PlanLine(line, writing ? Style.WARNING : isWritten ? Style.GOOD : Style.PLAIN));
                if (writing && !work.get().action().isBlank()) {
                    lines.add(new PlanLine(GOAL + Painter.fit(work.get().action(), Math.max(10, width - GOAL.length())), Style.WARNING));
                } else if (!isWritten && session.goal() != null && !session.goal().isBlank()) {
                    wrap(session.goal(), width - GOAL.length()).forEach(goal -> lines.add(new PlanLine(GOAL + goal, Style.DIM)));
                }
            }
        }
        return lines;
    }

    /** {@code writing 7/32}: what the agent does, and the sessions written. */
    private static String agentColumn(Watch watch, Track track) {
        if (watch.working()) {
            return spinner() + " writing " + track.sessions();
        }
        if (watch.others().stream().anyMatch(session -> session.state() == AgentBus.SessionState.WAITING)) {
            return "waiting " + track.sessions();
        }
        return watch.run().map(status -> switch (status.state()) {
            case RUNNING -> spinner() + " writing";
            case DONE -> "done";
            case FAILED -> "failed";
            case STOPPED -> "stopped";
        } + " " + track.sessions()).orElse("—");
    }

    /** {@code waits for chapter 1} when a chapter it needs is not written yet. */
    private static String waitsFor(SyllabusChapter chapter, ChapterLinks links, CourseSyllabus syllabus, Track track) {
        List<SyllabusChapter> chapters = syllabus.chapters();
        return links.after(chapter.id()).stream()
                .flatMap(id -> chapters.stream().filter(other -> other.id().equals(id)))
                .filter(other -> other.sessions().stream().anyMatch(session -> !track.written(other, session)))
                .map(other -> "waits for chapter " + (chapters.indexOf(other) + 1))
                .findFirst()
                .orElse("");
    }

    private static final String[] SPINNER = {"⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏"};

    /** A frame of the spinner, by the clock: the screen draws twenty times a second. */
    static String spinner() {
        return SPINNER[(int) (System.currentTimeMillis() / 100 % SPINNER.length)];
    }

    /** The bar of work under way: the cell being filled blinks. */
    static String movingBar(int done, int total, int width) {
        String bar = bar(done, total, width);
        int next = bar.indexOf('░');
        if (next < 0 || System.currentTimeMillis() / 400 % 2 == 0) {
            return bar;
        }
        return bar.substring(0, next) + "▒" + bar.substring(next + 1);
    }

    static String bar(int done, int total, int width) {
        int full = total == 0 ? 0 : (int) Math.round(width * (double) done / total);
        return "█".repeat(full) + "░".repeat(width - full);
    }

    /** A session on the bus: Claude Code opened by hand, or any agent that calls {@code boussole bus}. */
    private static void busLines(AgentBus.Session session, List<PlanLine> lines) {
        String since = LocalTime.ofInstant(session.started(), ZoneId.systemDefault()).withSecond(0).withNano(0).toString();
        String tokens = session.tokens().total() > 0 ? " · " + AgentRun.count(session.tokens().total()) + " tokens" : "";
        String head = switch (session.state()) {
            case WORKING -> spinner() + " " + session.tool() + " working since " + since + " · "
                    + duration((int) Duration.between(session.started(), Instant.now()).toMinutes());
            case WAITING -> "◇ " + session.tool() + " waiting for you · since " + since;
            case ENDED -> "✓ " + session.tool() + " ended · " + duration((int) Duration.between(session.started(), session.last()).toMinutes());
        };
        lines.add(new PlanLine(head + tokens, switch (session.state()) {
            case WORKING -> Style.WARNING;
            case WAITING -> Style.PLAIN;
            case ENDED -> Style.DIM;
        }));
        if (session.state() == AgentBus.SessionState.WORKING && !session.action().isBlank()) {
            lines.add(new PlanLine("  " + oneLine(session.action()), Style.DIM));
        }
        List<AgentBus.Agent> agents = session.agents();
        for (int index = 0; index < agents.size(); index++) {
            AgentBus.Agent agent = agents.get(index);
            boolean last = index == agents.size() - 1;
            boolean live = agent.ended().isEmpty() && session.state() != AgentBus.SessionState.ENDED;
            String id = agent.id().length() > 8 ? agent.id().substring(0, 8) : agent.id();
            Instant end = agent.ended().orElse(live ? Instant.now() : session.last());
            String numbers = (live ? "" : "done · ") + duration((int) Duration.between(agent.started(), end).toMinutes())
                    + (agent.tokens().total() > 0 ? " · " + AgentRun.count(agent.tokens().total()) + " tokens" : "");
            lines.add(new PlanLine((last ? "└─ " : "├─ ") + (live ? spinner() : "✓") + " " + id + "  "
                    + (agent.type().isBlank() ? "" : agent.type() + "  ") + agent.description() + "  " + numbers,
                    live ? Style.WARNING : Style.GOOD));
            if (live && !agent.action().isBlank()) {
                lines.add(new PlanLine((last ? "   " : "│  ") + "  " + oneLine(agent.action()), Style.DIM));
            }
        }
        lines.add(new PlanLine("", Style.PLAIN));
    }

    private static void agentLines(AgentRun.Status status, List<PlanLine> lines) {
        boolean live = status.state() == AgentRun.State.RUNNING;
        Style style = switch (status.state()) {
            case RUNNING -> Style.WARNING;
            case DONE -> Style.GOOD;
            case FAILED -> Style.ERROR;
            case STOPPED -> Style.DIM;
        };
        String time = live
                ? (status.started().getEpochSecond() > 0 ? " since " + LocalTime.ofInstant(status.started(), ZoneId.systemDefault())
                        .withNano(0).withSecond(0) + " · " + duration((int) Duration.between(status.started(), Instant.now()).toMinutes()) : "")
                : status.took().map(took -> " · " + duration((int) took.toMinutes())).orElse("");
        String state = switch (status.state()) {
            case RUNNING -> spinner() + " Agent running";
            case DONE -> "✓ Agent done";
            case FAILED -> "✗ Agent failed";
            case STOPPED -> "■ Agent stopped";
        };
        AgentRun.Tokens tokens = status.tokens();
        lines.add(new PlanLine(state + time + status.costUsd().map(cost -> String.format(Locale.ROOT, " · $%.2f", cost)).orElse("")
                + " · " + AgentRun.count(tokens.total()) + " tokens"
                + (tokens.cacheRead() > 0 ? " (" + AgentRun.count(tokens.cacheRead()) + " reread from the cache)" : ""), style));
        status.limit().ifPresent(limit -> {
            String resets = limit.resets().map(at -> ", resets at " + LocalTime.ofInstant(at, ZoneId.systemDefault()).withSecond(0).withNano(0))
                    .orElse("");
            boolean reset = limit.resets().map(at -> at.isBefore(Instant.now())).orElse(false);
            if (limit.reached() && reset) {
                lines.add(new PlanLine("Stopped by the usage limit, which has reset since.", Style.DIM));
            } else if (limit.reached()) {
                lines.add(new PlanLine("Usage limit reached" + resets + ".", Style.ERROR));
            } else if (live && limit.used() >= 0.5) {
                lines.add(new PlanLine(String.format(Locale.ROOT, "%d%% of the usage limit used%s.", Math.round(limit.used() * 100), resets),
                        limit.used() >= 0.9 ? Style.WARNING : Style.DIM));
            }
        });
        if (live && !status.action().isBlank()) {
            lines.add(new PlanLine("  " + status.action(), Style.DIM));
        }
        List<AgentRun.Task> tasks = status.tasks();
        for (int index = 0; index < tasks.size(); index++) {
            AgentRun.Task task = tasks.get(index);
            boolean last = index == tasks.size() - 1;
            String indent = "  ".repeat(Math.max(0, task.depth() - 1));
            String mark = switch (task.state()) {
                case RUNNING -> spinner();
                case DONE -> "✓";
                case FAILED -> "✗";
                case STOPPED -> "■";
            };
            String id = task.id().length() > 8 ? task.id().substring(0, 8) : task.id();
            String numbers = (switch (task.state()) {
                        case RUNNING -> "";
                        case DONE -> "done · ";
                        case FAILED -> "failed · ";
                        case STOPPED -> "stopped · ";
                    })
                    + duration((int) task.took().toMinutes()) + " · " + count(task.tools(), "action") + " · "
                    + AgentRun.count(task.tokens()) + " tokens";
            lines.add(new PlanLine(indent + (last ? "└─ " : "├─ ") + mark + " " + id + "  " + task.description() + "  " + numbers,
                    switch (task.state()) {
                        case RUNNING -> Style.WARNING;
                        case DONE -> Style.GOOD;
                        case FAILED -> Style.ERROR;
                        case STOPPED -> Style.DIM;
                    }));
            boolean failed = task.state() == AgentRun.TaskState.FAILED;
            if ((task.state() == AgentRun.TaskState.RUNNING || failed) && !task.action().isBlank()) {
                lines.add(new PlanLine(indent + (last ? "   " : "│  ") + "  " + oneLine(task.action()), failed ? Style.ERROR : Style.DIM));
            }
        }
        if (!status.refused().isEmpty()) {
            lines.add(new PlanLine("Refused: " + String.join(", ", status.refused()), Style.WARNING));
        }
        switch (status.state()) {
            case DONE -> {
                if (!status.result().isBlank()) {
                    lines.add(new PlanLine(oneLine(status.result()), Style.DIM));
                }
            }
            case FAILED -> {
                if (!status.result().isBlank()) {
                    lines.add(new PlanLine(oneLine(status.result()), Style.ERROR));
                }
                lines.add(new PlanLine("Log: " + Painter.folder(status.log()), Style.DIM));
            }
            case STOPPED -> lines.add(new PlanLine("Log: " + Painter.folder(status.log()), Style.DIM));
            case RUNNING -> {
            }
        }
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

    /** Draws the plan from its {@code scroll}-th line, at most up to its end; returns where it started. */
    private static int drawPlan(Painter painter, Track track, Watch watch, int scroll, int top, int width,
                                int height) {
        painter.pane(0, top, width, height, track.name() + " · Plan", false);
        int inner = width - 4;
        List<PlanLine> lines = planLines(track, watch, inner);
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
        return first;
    }

    private static void drawDocuments(Painter painter, Track track, DocumentPick pick, int column, int top, int width, int height) {
        if (pick != null) {
            drawPick(painter, pick, column, top, width, height);
            return;
        }
        painter.pane(column, top, width, height, track.locked() ? "Documents · locked" : "Documents", false);
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

    /** The documents to take in, each with its box, the cursor's line reversed, kept in view. */
    private static void drawPick(Painter painter, DocumentPick pick, int column, int top, int width, int height) {
        painter.pane(column, top, width, height, "Documents to take in", true);
        int inner = width - 4;
        int rows = Math.max(1, height - 2);
        int first = Math.max(0, pick.cursor() - rows + 1);
        int row = top + 1;
        for (int index = first; index < pick.documents().size() && row < top + height - 1; index++) {
            SourceState document = pick.documents().get(index);
            String line = (pick.ticked(document) ? "[x] " : "[ ] ") + Painter.column(document.state().name().toLowerCase(Locale.ROOT), 8)
                    + document.file();
            painter.text(column + 2, row++, line, inner, index == pick.cursor() ? Style.SELECTED : Style.PLAIN);
        }
    }

    @Override
    public boolean key(KeyStroke key) {
        if (pick != null) {
            pickKey(key);
            return true;
        }
        List<Track> read = tracks;
        if (key instanceof MouseAction mouse) {
            switch (mouse.getActionType()) {
                case SCROLL_DOWN -> scroll(WHEEL);
                case SCROLL_UP -> scroll(-WHEEL);
                default -> {
                    return false;
                }
            }
            return true;
        }
        if (key.getKeyType() == KeyType.Character && key.getCharacter() == ' ') {
            scroll(key.isShiftDown() ? -page() : page());
            return true;
        }
        switch (key.getKeyType()) {
            case ArrowUp -> {
                selected = Math.max(0, selected - 1);
                scroll = 0;
            }
            case ArrowDown -> {
                selected = read == null ? 0 : Math.min(Math.max(0, read.size() - 1), selected + 1);
                scroll = 0;
            }
            case PageDown -> scroll(page());
            case PageUp -> scroll(-page());
            case Character -> {
                return character(key.getCharacter());
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    /** A page of the plan, or of the problems, which take two lines each. */
    private int page() {
        return plan ? PAGE * 2 : PAGE;
    }

    /** Moves the plan or the problems by {@code by} lines or problems, within what there is. */
    private void scroll(int by) {
        int last = chosen().map(track -> (plan ? planLines(track, watch(track), PLAN_WIDTH).size() : track.diagnostics().size()) - 1)
                .orElse(0);
        scroll = Math.max(0, Math.min(scroll + by, Math.max(0, last)));
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
            case 'L' -> track.ifPresent(this::lock);
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
            app.say(track.course().isEmpty() ? "No plan to approve." : "The plan of " + track.name() + " is already approved.");
            return;
        }
        plan = true;
        app.confirm("Approve the plan of " + track.name() + "?", () -> {
            PlanApproval.approve(track.folder());
            check("Plan of " + track.name() + " approved.");
        });
    }

    /** While ticking documents: move, tick, take them in, or write the sessions left without them. */
    private void pickKey(KeyStroke key) {
        Optional<Track> track = (tracks == null ? List.<Track>of() : tracks).stream()
                .filter(found -> found.folder().equals(pick.track())).findFirst();
        switch (key.getKeyType()) {
            case ArrowUp -> pick.move(-1);
            case ArrowDown -> pick.move(1);
            case Escape -> pick = null;
            case Enter -> {
                if (pick.chosen().isEmpty()) {
                    app.say("Tick a document with Space, or a to tick them all.");
                } else {
                    Set<String> chosen = pick.chosen();
                    pick = null;
                    track.ifPresent(found -> start(found, AgentRun.TAKE_IN, chosen,
                            "Take " + count(chosen.size(), "document") + " into " + found.name() + " with Claude Code?"));
                }
            }
            case Character -> {
                switch (key.getCharacter()) {
                    case ' ' -> pick.toggle();
                    case 'a' -> pick.toggleAll();
                    case 'w' -> {
                        pick = null;
                        track.filter(found -> found.left() > 0).ifPresentOrElse(found -> start(found, AgentRun.PROMPT, Set.of(),
                                        "Write the " + count((int) found.left(), "session") + " left of " + found.name()
                                                + " with Claude Code, without the new documents?"),
                                () -> app.say("No session left to write."));
                    }
                    default -> {
                    }
                }
            }
            default -> {
            }
        }
    }

    private void run(Track track) {
        if (agent(track).map(status -> status.state() == AgentRun.State.RUNNING).orElse(false)) {
            app.say("An agent is already writing " + track.name() + ".");
        } else if (!track.approved()) {
            app.say(track.course().isEmpty() ? "No plan here." : "Approve the plan first.");
        } else if (track.toTakeIn() > 0) {
            // the learner chooses the documents: the sessions left may change with them
            pick = new DocumentPick(track.folder(), TrackBrief.toTakeIn(track.course().orElseThrow(), track.documents()));
            plan = false;
        } else if (track.left() == 0) {
            app.say(track.course().flatMap(TrackBrief::finalExamWaiting).isPresent()
                    ? "The final mock exam waits: L locks " + track.name() + " once every document is in."
                    : "Every session of " + track.name() + " is written.");
        } else {
            start(track, AgentRun.PROMPT, Set.of(), "Write the " + count((int) track.left(), "session") + " left of " + track.name()
                    + " with Claude Code?");
        }
    }

    /** Starts the agent on {@code task}, the brief of the track in its prompt, once the learner says yes. */
    private void start(Track track, String task, Set<String> documents, String question) {
        Optional<String> problem = System.getProperty("boussole.agent") == null
                ? io.github.lucasssoh.boussole.cli.init.Sandbox.problem() : Optional.empty();
        if (problem.isPresent()) {
            app.error("No agent without its sandbox: " + problem.get() + ".");
            return;
        }
        plan = true;
        app.confirm(question, () -> app.job("Starting the agent…", () -> {
            String jar = System.getProperty("boussole.jar");
            String agent = System.getProperty("boussole.agent");
            String brief = track.course().map(course -> TrackBrief.of(course, track.documents(), documents)).orElse("");
            List<String> command = AgentRun.command(agent == null ? "claude" : agent, AgentRun.prompt(task, brief));
            // a stand-in agent (tests) does not notify the desktop
            io.github.lucasssoh.boussole.cli.init.ClaudeHooks.install(track.folder());
            Path log = AgentRun.start(track.folder(), agent == null ? AgentRun.notifying(track.name(), jar, command) : command);
            agents.put(track.folder(), AgentRun.read(log));
            return "Agent started on " + track.name() + ".";
        }));
    }

    /**
     * Locks the track, or unlocks it. Unlocking once the final mock exam is written warns that a
     * document taken in then means building it again, with what a session costs on this track.
     */
    private void lock(Track track) {
        if (track.course().map(course -> course.syllabus().chapters().isEmpty()).orElse(true)) {
            app.say("No plan to lock.");
        } else if (track.locked()) {
            String cost = track.course().filter(TrackBrief::finalExamWritten)
                    .map(course -> " A document taken in then means building the final mock exam again"
                            + AgentRun.perSession(track.folder(), course.sessions().size())
                            .map(tokens -> ": about " + AgentRun.count(tokens) + " tokens, a session's average here.").orElse("."))
                    .orElse("");
            app.confirm("Unlock " + track.name() + "?" + cost, () -> {
                TrackLock.unlock(track.folder());
                check(track.name() + " unlocked.");
            });
        } else {
            app.confirm("Lock " + track.name() + "? Documents added from now on are left out, and the final mock exam can be written.", () -> {
                TrackLock.lock(track.folder(), java.time.LocalDate.now());
                check(track.name() + " locked.");
            });
        }
    }

    private void stop(Track track) {
        Optional<AgentRun.Status> running = agent(track).filter(status -> status.state() == AgentRun.State.RUNNING);
        if (running.isEmpty()) {
            app.say("No agent is writing " + track.name() + ".");
            return;
        }
        app.confirm("Stop the agent writing " + track.name() + "?", () -> {
            AgentRun.stop(running.get().log());
            app.say("Agent stopped.");
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
        if (pick != null) {
            return List.of(new Hint("Space", "Tick"), new Hint("a", "All"), new Hint("Enter", "Take in"),
                    new Hint("w", "Sessions only"), new Hint("Esc", "Cancel"));
        }
        Optional<Track> chosen = chosen();
        boolean proposed = chosen.map(Track::proposed).orElse(false);
        boolean running = chosen.flatMap(this::agent).map(status -> status.state() == AgentRun.State.RUNNING).orElse(false);
        List<Hint> hints = new ArrayList<>(List.of(new Hint("v", "Validate"), new Hint("p", plan ? "Problems" : "Plan"),
                new Hint("Space", "Scroll")));
        if (proposed) {
            hints.add(new Hint("a", "Approve"));
        }
        if (running) {
            hints.add(new Hint("x", "Stop agent"));
        } else if (chosen.map(track -> track.approved() && track.toTakeIn() > 0).orElse(false)) {
            hints.add(new Hint("r", "Take in documents"));
        } else if (chosen.map(track -> track.approved() && track.left() > 0).orElse(false)) {
            hints.add(new Hint("r", "Run agent"));
        }
        if (chosen.map(Track::approved).orElse(false)) {
            hints.add(new Hint("L", chosen.get().locked() ? "Unlock" : "Lock"));
        }
        hints.addAll(List.of(new Hint("o", "Open"), new Hint("f", "Folder"), new Hint("+", "Add"), new Hint("-", "Remove")));
        return hints;
    }
}
