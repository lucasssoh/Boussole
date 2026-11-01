package io.github.lucasssoh.boussole.cli.admin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * An agent that writes the sessions of an approved plan: Claude Code run with {@code --print} in
 * the folder of the track, its events kept in {@code .boussole/runs/<time>.jsonl}, so that the
 * Tracks screen shows where it stands and the tokens it spent, even after {@code boussole} closed
 * and opened again. The agent may change the files of the track, run {@code boussole validate} and
 * {@code boussole index}, read the documents ({@code pdftotext}, {@code pdfinfo}, {@code ls}) and
 * run {@code python3} to check an answer, nothing else.
 */
final class AgentRun {

    static final String PROMPT = """
            The plan of this track is approved (cursus/syllabus.yaml, status: approved). Follow AGENTS.md: \
            write the sessions that have no file yet, in the order of the plan, with the prerequisites the \
            plan gives them; one agent per chapter, three at a time at most, each writing only in its \
            chapter's folder. Write in the language of the track. You cannot ask the learner anything: \
            when a choice is theirs, take the safest one and say so in your report. End with \
            `boussole validate .` without errors, then a short report: what was written, what you \
            assumed, what is left.""";

    private static final ObjectMapper JSON = new ObjectMapper();
    /** What each log said at a size: the screen asks every second, the log grows only while the agent runs. */
    private static final Map<Path, Read> READ = new ConcurrentHashMap<>();

    private record Read(long size, Status status) {
    }

    private static final DateTimeFormatter NAME = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss", Locale.ROOT);

    /** Where a run stands. */
    enum State {
        RUNNING, DONE, FAILED, STOPPED
    }

    /** Tokens as Claude counts them: read from the cache, written to it, and the rest. */
    record Tokens(long input, long output, long cacheRead, long cacheWrite) {

        static final Tokens NONE = new Tokens(0, 0, 0, 0);

        long total() {
            return input + output + cacheRead + cacheWrite;
        }

        Tokens plus(Tokens other) {
            return new Tokens(input + other.input, output + other.output, cacheRead + other.cacheRead, cacheWrite + other.cacheWrite);
        }
    }

    /** A run as its file tells it. */
    record Status(Path log, State state, Tokens tokens, Optional<Double> costUsd, Instant started, Optional<Duration> took,
                  List<String> recent, String result, Map<String, Chapter> chapters, List<Task> tasks, List<String> refused,
                  Optional<Limit> limit, String sessionId) {

        Status(Path log, State state, Tokens tokens, Optional<Double> costUsd, Instant started, Optional<Duration> took,
               List<String> recent, String result) {
            this(log, state, tokens, costUsd, started, took, recent, result, Map.of(), List.of(), List.of(), Optional.empty(), "");
        }

        /** The last action of the agent that coordinates. */
        String action() {
            return recent.isEmpty() ? "" : recent.getLast();
        }

        /** What happens in the chapter of this folder, if an agent works in it. */
        Optional<Chapter> chapter(String dir) {
            return Optional.ofNullable(chapters.get(dir));
        }

    }

    /**
     * An agent the coordinating agent started: its {@code id}, what it was asked, how deep it sits
     * (1 under the coordinating agent), where it stands, and the chapter folder it writes in, once
     * it wrote there.
     */
    record Task(String id, String description, int depth, TaskState state, String action, long tokens, int tools,
                Duration took, Optional<String> folder) {
    }

    /** The usage limit of the account, as Claude Code last saw it: the share used, reached or not, when it resets. */
    record Limit(String window, double used, boolean reached, Optional<Instant> resets) {
    }

    enum TaskState {
        RUNNING, DONE, FAILED, STOPPED
    }

    /** The work in a chapter folder: the session file touched last and the last action there. */
    record Chapter(String file, String action) {
    }

    private AgentRun() {
    }

    static Path runs(Path track) {
        return track.resolve(".boussole").resolve("runs");
    }

    /** The command that starts the agent: {@code agent} is {@code claude}, or a stand-in in tests. */
    static List<String> command(String agent, String jar) {
        List<String> command = new ArrayList<>(List.of(agent, "--print", PROMPT, "--output-format", "stream-json", "--verbose",
                "--permission-mode", "acceptEdits", "--allowedTools",
                "Read", "Write", "Edit", "Glob", "Grep", "Agent", "Task", "TodoWrite",
                "Bash(boussole validate:*)", "Bash(boussole index:*)",
                // to read the documents, and to check an answer by computing it
                "Bash(pdftotext:*)", "Bash(pdfinfo:*)", "Bash(ls:*)", "Bash(python3:*)"));
        if (jar != null && !jar.isBlank()) {
            command.add("Bash(java -jar " + jar + " validate:*)");
            command.add("Bash(java -jar " + jar + " index:*)");
        }
        return List.copyOf(command);
    }

    /**
     * Once the agent ends, a notification of the desktop ({@code notify-send}, when there is one)
     * says how many sessions are written, so that {@code boussole} need not stay open. {@code $1}
     * is the name of the track, {@code $2} the jar of Boussole or empty; the agent follows.
     */
    private static final String NOTIFY = """
            name=$1; jar=$2; shift 2
            "$@"; code=$?
            if command -v notify-send >/dev/null 2>&1; then
              if [ -n "$jar" ]; then report=$(java -jar "$jar" validate . 2>/dev/null | tail -n 1)
              else report=$(boussole validate . 2>/dev/null | tail -n 1); fi
              written=${report##*; }
              if [ "$code" -eq 0 ]; then notify-send -a Boussole "$name" "$written"
              else notify-send -a Boussole "$name" "Agent stopped. $written"; fi
            fi
            exit $code
            """;

    /** The agent, then the notification of its end. */
    static List<String> notifying(String name, String jar, List<String> agent) {
        List<String> command = new ArrayList<>(List.of("sh", "-c", NOTIFY, "boussole-agent", name, jar == null ? "" : jar));
        command.addAll(agent);
        return List.copyOf(command);
    }

    /** Starts the agent in {@code track}; it goes on when {@code boussole} closes. */
    static Path start(Path track, List<String> command) {
        try {
            Path runs = Files.createDirectories(runs(track));
            String name = LocalDateTime.now().format(NAME);
            Path log = runs.resolve(name + ".jsonl");
            ProcessBuilder builder = new ProcessBuilder(command).directory(track.toFile());
            // the agents of each chapter run in the background: wait for them, however long they take
            builder.environment().put("CLAUDE_CODE_PRINT_BG_WAIT_CEILING_MS", "0");
            Process process = builder
                    .redirectOutput(log.toFile())
                    .redirectError(runs.resolve(name + ".err").toFile())
                    .redirectInput(ProcessBuilder.Redirect.from(Path.of("/dev/null").toFile()))
                    .start();
            Files.writeString(runs.resolve(name + ".pid"), String.valueOf(process.pid()));
            return log;
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot start " + command.getFirst() + ": " + e.getMessage(), e);
        }
    }

    /** Stops the running agent of this log, and the agents it started. */
    static boolean stop(Path log) {
        return process(log).map(handle -> {
            handle.descendants().forEach(ProcessHandle::destroy);
            return handle.destroy();
        }).orElse(false);
    }

    private static Optional<ProcessHandle> process(Path log) {
        Path pid = sibling(log, ".pid");
        try {
            return Files.isRegularFile(pid)
                    ? ProcessHandle.of(Long.parseLong(Files.readString(pid).strip())).filter(ProcessHandle::isAlive)
                    : Optional.empty();
        } catch (IOException | NumberFormatException e) {
            return Optional.empty();
        }
    }

    /** The last run of the track, if it has one. */
    static Optional<Status> last(Path track) {
        Path runs = runs(track);
        if (!Files.isDirectory(runs)) {
            return Optional.empty();
        }
        try (Stream<Path> files = Files.list(runs)) {
            return files.filter(file -> file.getFileName().toString().endsWith(".jsonl"))
                    .max(Comparator.comparing(file -> file.getFileName().toString()))
                    .map(AgentRun::cached);
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private static Status cached(Path log) {
        long size;
        try {
            size = Files.size(log);
        } catch (IOException e) {
            return read(log);
        }
        Read known = READ.get(log);
        if (known != null && known.size() == size
                && (known.status().state() != State.RUNNING || process(log).isPresent())) {
            return known.status();
        }
        Status status = read(log);
        READ.put(log, new Read(size, status));
        return status;
    }

    /** Reads the events of a run: the tokens of each message once, its last actions, its result. */
    static Status read(Path log) {
        Tokens tokens = Tokens.NONE;
        Set<String> counted = new HashSet<>();
        Map<String, String> folderOf = new HashMap<>();
        Map<String, Chapter> chapters = new LinkedHashMap<>();
        Map<String, TaskLog> tasks = new LinkedHashMap<>();
        Map<String, String> taskOfToolUse = new HashMap<>();
        Set<String> active = null;
        Map<String, String> commands = new HashMap<>();
        Set<String> refused = new LinkedHashSet<>();
        Limit limit = null;
        List<String> recent = new ArrayList<>();
        Optional<Double> cost = Optional.empty();
        Optional<Duration> took = Optional.empty();
        Tokens finalTokens = null;
        State ended = null;
        String result = "";
        String sessionId = "";
        try (BufferedReader lines = Files.newBufferedReader(log, StandardCharsets.UTF_8)) {
            for (String line = lines.readLine(); line != null; line = lines.readLine()) {
                JsonNode event;
                try {
                    event = JSON.readTree(line);
                } catch (RuntimeException notJson) {
                    continue;
                }
                String type = event.path("type").asString("");
                if (sessionId.isEmpty()) {
                    sessionId = event.path("session_id").asString("");
                }
                if (type.equals("assistant")) {
                    JsonNode message = event.path("message");
                    String id = message.path("id").asString("");
                    if (!id.isEmpty() && counted.add(id)) {
                        tokens = tokens.plus(tokens(message.path("usage")));
                    }
                    String agent = event.path("parent_tool_use_id").asString("main");
                    for (JsonNode part : message.path("content")) {
                        if (part.path("type").asString("").equals("tool_use")) {
                            commands.put(part.path("id").asString(""), program(part));
                        }
                        chapterFile(part).ifPresent(file -> {
                            folderOf.put(agent, file[0]);
                            chapters.put(file[0], new Chapter(file[1], chapters.containsKey(file[0]) ? chapters.get(file[0]).action() : ""));
                        });
                        String folder = folderOf.get(agent);
                        if (folder != null && !agent.equals("main")) {
                            action(part).ifPresent(action -> chapters.put(folder, new Chapter(chapters.get(folder).file(), action)));
                        }
                        action(part).ifPresent(action -> {
                            recent.add(action);
                            if (recent.size() > 6) {
                                recent.removeFirst();
                            }
                        });
                    }
                } else if (type.equals("system")) {
                    String id = event.path("task_id").asString("");
                    switch (event.path("subtype").asString("")) {
                        case "task_started" -> {
                            TaskLog task = new TaskLog(id, event.path("description").asString(""), event.path("spawn_depth").asInt(1));
                            tasks.put(id, task);
                            taskOfToolUse.put(event.path("tool_use_id").asString(""), id);
                        }
                        case "task_progress" -> {
                            TaskLog task = tasks.get(id);
                            if (task != null) {
                                task.action = event.path("description").asString(task.action);
                                task.tokens = event.path("usage").path("total_tokens").asLong(task.tokens);
                                task.tools = event.path("usage").path("tool_uses").asInt(task.tools);
                                task.millis = event.path("usage").path("duration_ms").asLong(task.millis);
                            }
                        }
                        case "background_tasks_changed" -> {
                            active = new HashSet<>();
                            for (JsonNode task : event.path("tasks")) {
                                active.add(task.path("task_id").asString(""));
                            }
                            for (TaskLog task : tasks.values()) {
                                if (!task.ended && !active.contains(task.id)) {
                                    task.ended = true;
                                    task.cut = limit != null && limit.reached();
                                }
                            }
                        }
                        case "permission_denied" -> refused.add(commands.getOrDefault(event.path("tool_use_id").asString(""),
                                event.path("tool_name").asString("")));
                        case "task_updated", "task_notification" -> {
                            TaskLog task = tasks.get(id);
                            JsonNode said = event.has("patch") ? event.path("patch") : event;
                            String outcome = said.path("status").asString("");
                            if (task != null && !outcome.isEmpty() && !outcome.equals("running")) {
                                task.ended = true;
                                task.status = outcome;
                                if (!said.path("error").asString("").isBlank()) {
                                    task.error = said.path("error").asString("");
                                }
                            }
                        }
                        default -> {
                        }
                    }
                } else if (type.equals("rate_limit_event")) {
                    JsonNode info = event.path("rate_limit_info");
                    String window = info.path("rateLimitType").asString("");
                    JsonNode windowInfo = info.path("unifiedWindows").path(window);
                    long resets = info.path("resetsAt").asLong(0);
                    limit = new Limit(window, windowInfo.path("utilization").asDouble(info.path("utilization").asDouble(0)),
                            info.path("status").asString("").equals("rejected"),
                            resets > 0 ? Optional.of(Instant.ofEpochSecond(resets)) : Optional.empty());
                } else if (type.equals("result")) {
                    ended = event.path("is_error").asBoolean(false) || !event.path("subtype").asString("success").equals("success")
                            ? State.FAILED : State.DONE;
                    String said = event.path("result").asString("");
                    if (!said.isBlank()) {
                        result = said;
                    }
                    if (event.has("total_cost_usd")) {
                        cost = Optional.of(event.path("total_cost_usd").asDouble());
                    }
                    finalTokens = modelTokens(event.path("modelUsage"));
                }
            }
        } catch (IOException e) {
            return new Status(log, State.FAILED, tokens, cost, started(log), took, recent, e.getMessage());
        }
        if (finalTokens != null && finalTokens.total() > 0) {
            tokens = finalTokens;
        }
        boolean alive = process(log).isPresent();
        State state = alive ? State.RUNNING : ended != null ? ended : State.STOPPED;
        if (!alive) {
            took = elapsed(log);
        }
        // Claude Code says so when it stopped the agents of the chapters before they were done, and
        // ends with success when the usage limit stopped it
        if (state == State.DONE && (errors(log).contains("Background tasks still running") || limit != null && limit.reached())) {
            state = State.STOPPED;
        }
        if (state == State.STOPPED && tokens.total() == 0 && limit == null) {
            state = State.FAILED;
            result = firstLine(sibling(log, ".err"));
        }
        boolean live = state == State.RUNNING;
        List<Task> started = tasks.values().stream()
                .map(task -> {
                    String toolUse = taskOfToolUse.entrySet().stream().filter(entry -> entry.getValue().equals(task.id))
                            .map(Map.Entry::getKey).findFirst().orElse("");
                    return new Task(task.id, task.description, task.depth,
                            !task.ended ? live ? TaskState.RUNNING : TaskState.STOPPED
                                    : task.status.equals("failed") ? TaskState.FAILED
                                    : task.cut || task.status.equals("killed") || task.status.equals("stopped") ? TaskState.STOPPED
                                    : TaskState.DONE,
                            task.error.isBlank() ? task.action : task.error, task.tokens, task.tools, Duration.ofMillis(task.millis), Optional.ofNullable(folderOf.get(toolUse)));
                })
                .toList();
        return new Status(log, state, tokens, cost, started(log), took, List.copyOf(recent), result, Map.copyOf(chapters), started,
                List.copyOf(refused), Optional.ofNullable(limit), sessionId);
    }

    /** What a task said of itself, as its events come. */
    private static final class TaskLog {
        final String id;
        final String description;
        final int depth;
        String action = "";
        long tokens;
        int tools;
        long millis;
        boolean ended;
        /** Ended once the usage limit was reached: stopped, not done. */
        boolean cut;
        /** What the task said when it ended: {@code completed}, {@code failed}, {@code killed}. */
        String status = "";
        String error = "";

        TaskLog(String id, String description, int depth) {
            this.id = id;
            this.description = description;
            this.depth = depth;
        }
    }

    /** The program a command runs, past the {@code cd … &&} that leads to it: {@code pdftotext}. */
    static String program(JsonNode part) {
        String name = part.path("name").asString("");
        String command = part.path("input").path("command").asString("");
        if (!name.equals("Bash") || command.isBlank()) {
            return name;
        }
        for (String piece : command.split("&&|;|\\|\\|")) {
            String word = piece.strip().split("\\s+")[0];
            if (!word.isEmpty() && !word.equals("cd")) {
                return word;
            }
        }
        return name;
    }

    /** The chapter folder and the session file a write or an edit touches: {@code cursus/<folder>/<file>.md}. */
    static Optional<String[]> chapterFile(JsonNode part) {
        if (!part.path("type").asString("").equals("tool_use")) {
            return Optional.empty();
        }
        String path = part.path("input").path("file_path").asString("").replace('\\', '/');
        int cursus = path.lastIndexOf("cursus/");
        if (cursus < 0 || !path.endsWith(".md")) {
            return Optional.empty();
        }
        String[] parts = path.substring(cursus + "cursus/".length()).split("/");
        return parts.length == 2 ? Optional.of(parts) : Optional.empty();
    }

    static Tokens tokens(JsonNode usage) {
        return new Tokens(usage.path("input_tokens").asLong(0), usage.path("output_tokens").asLong(0),
                usage.path("cache_read_input_tokens").asLong(0), usage.path("cache_creation_input_tokens").asLong(0));
    }

    /** The totals of every model of the session, the agents it started included. */
    private static Tokens modelTokens(JsonNode models) {
        Tokens total = Tokens.NONE;
        for (JsonNode model : models) {
            total = total.plus(new Tokens(model.path("inputTokens").asLong(0), model.path("outputTokens").asLong(0),
                    model.path("cacheReadInputTokens").asLong(0), model.path("cacheCreationInputTokens").asLong(0)));
        }
        return total;
    }

    /** What a tool use does, in a line: {@code Read cursus/syllabus.yaml}. */
    static Optional<String> describe(JsonNode part) {
        return action(part).filter(line -> part.path("type").asString("").equals("tool_use"));
    }

    private static Optional<String> action(JsonNode part) {
        return switch (part.path("type").asString("")) {
            case "tool_use" -> {
                JsonNode input = part.path("input");
                String about = Stream.of("description", "file_path", "command", "pattern")
                        .map(key -> input.path(key).asString(""))
                        .filter(value -> !value.isBlank())
                        .findFirst()
                        .orElse("");
                yield Optional.of(part.path("name").asString("tool") + (about.isEmpty() ? "" : " " + oneLine(about)));
            }
            case "text" -> Optional.of(oneLine(part.path("text").asString(""))).filter(text -> !text.isBlank());
            default -> Optional.empty();
        };
    }

    private static String oneLine(String text) {
        return text.strip().replaceAll("\\s+", " ");
    }

    private static Path sibling(Path log, String extension) {
        String name = log.getFileName().toString();
        return log.resolveSibling(name.substring(0, name.length() - ".jsonl".length()) + extension);
    }

    private static String errors(Path log) {
        try {
            Path file = sibling(log, ".err");
            return Files.isRegularFile(file) ? Files.readString(file) : "";
        } catch (IOException e) {
            return "";
        }
    }

    private static String firstLine(Path file) {
        try {
            return Files.isRegularFile(file) ? Files.readAllLines(file).stream().filter(line -> !line.isBlank()).findFirst().orElse("") : "";
        } catch (IOException e) {
            return "";
        }
    }

    /** From the start to the last event: the agents of the chapters included, which the result does not time. */
    private static Optional<Duration> elapsed(Path log) {
        try {
            Instant started = started(log);
            return started.equals(Instant.EPOCH) ? Optional.empty()
                    : Optional.of(Duration.between(started, Files.getLastModifiedTime(log).toInstant()));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private static Instant started(Path log) {
        try {
            return Files.getLastModifiedTime(sibling(log, ".pid")).toInstant();
        } catch (IOException e) {
            return Instant.EPOCH;
        }
    }

    /** {@code 1.2M}, {@code 340k}, {@code 512}. */
    static String count(long tokens) {
        if (tokens >= 1_000_000) {
            return String.format(Locale.ROOT, "%.1fM", tokens / 1_000_000.0);
        }
        return tokens >= 1_000 ? tokens / 1_000 + "k" : String.valueOf(tokens);
    }
}
