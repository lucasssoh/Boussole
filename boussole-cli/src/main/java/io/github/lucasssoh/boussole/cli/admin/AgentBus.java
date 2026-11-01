package io.github.lucasssoh.boussole.cli.admin;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Reads the bus of a track, {@code .boussole/bus/events.jsonl}, that the hooks of Claude Code fill
 * (see {@code ClaudeHooks}): every session that works in the folder, the agents it starts, what
 * each does, the session file each writes, and the tokens each spent, from their transcripts.
 */
final class AgentBus {

    /** A session with no news for this long, and no end, is taken as gone. */
    static final Duration GONE = Duration.ofMinutes(30);
    /** An ended session stays on screen this long. */
    static final Duration SHOWN = Duration.ofMinutes(15);

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Map<Path, Read> READ = new ConcurrentHashMap<>();

    /** Where a session stands: it works, it waits for the person who opened it, or it ended. */
    enum SessionState {
        WORKING, WAITING, ENDED
    }

    record Session(String id, String tool, Instant started, Instant last, SessionState state, String action, List<Agent> agents,
                   Map<String, AgentRun.Chapter> chapters, AgentRun.Tokens tokens) {
    }

    /** An agent a session started: its ID, its type, what it was asked, whether it ended, its last action. */
    record Agent(String id, String type, String description, Instant started, Optional<Instant> ended, String action,
                 AgentRun.Tokens tokens) {
    }

    private record Read(long size, List<Session> sessions) {
    }

    private AgentBus() {
    }

    static Path events(Path track) {
        return track.resolve(".boussole").resolve("bus").resolve("events.jsonl");
    }

    /** The sessions to show: those under way, and those that ended a short while ago. */
    static List<Session> sessions(Path track, Instant now) {
        Path events = events(track);
        long size;
        try {
            size = Files.isRegularFile(events) ? Files.size(events) : -1;
        } catch (IOException e) {
            size = -1;
        }
        if (size < 0) {
            return List.of();
        }
        Read known = READ.get(events);
        List<Session> all;
        if (known != null && known.size() == size) {
            all = known.sessions();
        } else {
            all = read(events, now);
            READ.put(events, new Read(size, all));
        }
        return all.stream()
                // a session without news ended GONE after its last one, and leaves SHOWN later
                .filter(session -> session.last().plus(session.state() == SessionState.ENDED ? SHOWN : GONE.plus(SHOWN)).isAfter(now))
                .map(session -> session.state() != SessionState.ENDED && session.last().plus(GONE).isBefore(now)
                        ? new Session(session.id(), session.tool(), session.started(), session.last(), SessionState.ENDED, session.action(),
                                session.agents(), session.chapters(), session.tokens())
                        : session)
                .toList();
    }

    private static final class Building {
        final String id;
        String tool = "";
        Instant started;
        Instant last;
        SessionState state = SessionState.WORKING;
        String action = "";
        String transcript = "";
        final Map<String, Agent> agents = new LinkedHashMap<>();
        final Map<String, String> folderOf = new LinkedHashMap<>();
        final Map<String, AgentRun.Chapter> chapters = new LinkedHashMap<>();
        final Deque<String> asked = new ArrayDeque<>();

        Building(String id, Instant at) {
            this.id = id;
            this.started = at;
            this.last = at;
        }
    }

    static List<Session> read(Path events, Instant now) {
        Map<String, Building> sessions = new LinkedHashMap<>();
        try (BufferedReader lines = Files.newBufferedReader(events, StandardCharsets.UTF_8)) {
            for (String line = lines.readLine(); line != null; line = lines.readLine()) {
                int space = line.indexOf(' ');
                if (space <= 0) {
                    continue;
                }
                Instant at;
                JsonNode event;
                try {
                    at = Instant.ofEpochSecond(Long.parseLong(line.substring(0, space)));
                    event = JSON.readTree(line.substring(space + 1));
                } catch (RuntimeException notAnEvent) {
                    continue;
                }
                boolean neutral = event.has("bus");
                String id = neutral ? event.path("tool").asString("agent") + ":" + event.path("session").asString("")
                        : event.path("session_id").asString("");
                if (id.isEmpty()) {
                    continue;
                }
                Building session = sessions.computeIfAbsent(id, key -> new Building(key, at));
                session.last = at;
                if (neutral) {
                    session.tool = event.path("tool").asString("agent");
                    said(session, event, at);
                } else {
                    session.tool = "Claude Code";
                    if (session.transcript.isEmpty()) {
                        session.transcript = event.path("transcript_path").asString("");
                    }
                    take(session, event, at);
                }
            }
        } catch (IOException e) {
            return List.of();
        }
        return sessions.values().stream().map(AgentBus::session).toList();
    }

    /** A line of {@code boussole bus}, which any agent writes. */
    private static void said(Building session, JsonNode event, Instant at) {
        String agent = event.path("agent").asString("");
        String text = event.path("text").asString("");
        String file = event.path("file").asString("");
        switch (event.path("event").asString("")) {
            case "start", "doing" -> {
                session.state = SessionState.WORKING;
                act(session, agent, text);
            }
            case "writing" -> {
                session.state = SessionState.WORKING;
                String action = text.isBlank() ? "Writing " + file : text;
                AgentRun.chapterFile(JSON.createObjectNode().put("type", "tool_use")
                                .set("input", JSON.createObjectNode().put("file_path", file)))
                        .ifPresent(found -> {
                            session.folderOf.put(agent, found[0]);
                            session.chapters.put(found[0], new AgentRun.Chapter(found[1], action));
                        });
                act(session, agent, action);
            }
            case "agent-start" -> session.agents.put(agent, new Agent(agent, "", text, at, Optional.empty(), "", AgentRun.Tokens.NONE));
            case "agent-end" -> session.agents.computeIfPresent(agent, (key, known) -> new Agent(known.id(), known.type(),
                    known.description(), known.started(), Optional.of(at), known.action(), known.tokens()));
            case "waiting" -> session.state = SessionState.WAITING;
            case "end" -> session.state = SessionState.ENDED;
            default -> {
            }
        }
    }

    /** The last action of the session, or of one of its agents, and of the chapter that agent writes in. */
    private static void act(Building session, String agent, String action) {
        if (action.isBlank()) {
            return;
        }
        String folder = session.folderOf.get(agent);
        if (folder != null && session.chapters.containsKey(folder)) {
            session.chapters.put(folder, new AgentRun.Chapter(session.chapters.get(folder).file(), action));
        }
        if (agent.isEmpty()) {
            session.action = action;
        } else {
            session.agents.computeIfPresent(agent, (key, known) -> new Agent(known.id(), known.type(), known.description(),
                    known.started(), known.ended(), action, known.tokens()));
        }
    }

    /** A line of the hooks of Claude Code. */
    private static void take(Building session, JsonNode event, Instant at) {
        String agent = event.path("agent_id").asString("");
        switch (event.path("hook_event_name").asString("")) {
            case "SessionStart" -> session.state = SessionState.WORKING;
            case "PreToolUse" -> {
                if (!agent.isEmpty() || !session.state.equals(SessionState.ENDED)) {
                    session.state = SessionState.WORKING;
                }
                JsonNode part = JSON.createObjectNode()
                        .put("type", "tool_use")
                        .put("name", event.path("tool_name").asString(""))
                        .set("input", event.path("tool_input"));
                String action = AgentRun.describe(part).orElse("");
                if (event.path("tool_name").asString("").matches("Agent|Task")) {
                    session.asked.addLast(event.path("tool_input").path("description").asString(""));
                }
                AgentRun.chapterFile(part).ifPresent(file -> {
                    session.folderOf.put(agent, file[0]);
                    session.chapters.put(file[0], new AgentRun.Chapter(file[1], action));
                });
                String folder = session.folderOf.get(agent);
                if (folder != null && session.chapters.containsKey(folder)) {
                    session.chapters.put(folder, new AgentRun.Chapter(session.chapters.get(folder).file(), action));
                }
                if (agent.isEmpty()) {
                    session.action = action;
                } else {
                    session.agents.computeIfPresent(agent, (key, known) -> new Agent(known.id(), known.type(), known.description(),
                            known.started(), known.ended(), action, known.tokens()));
                }
            }
            case "SubagentStart" -> session.agents.put(agent, new Agent(agent, event.path("agent_type").asString(""),
                    session.asked.isEmpty() ? "" : session.asked.removeFirst(), at, Optional.empty(), "", AgentRun.Tokens.NONE));
            case "SubagentStop" -> session.agents.computeIfPresent(agent, (key, known) -> new Agent(known.id(), known.type(),
                    known.description(), known.started(), Optional.of(at), known.action(), known.tokens()));
            case "Stop" -> {
                // with agents still running in the background, the session goes on working
                boolean background = false;
                for (JsonNode task : event.path("background_tasks")) {
                    background |= task.path("status").asString("").equals("running");
                }
                session.state = background ? SessionState.WORKING : SessionState.WAITING;
            }
            case "SessionEnd" -> session.state = SessionState.ENDED;
            default -> {
            }
        }
    }

    private static Session session(Building session) {
        Path transcript = session.transcript.isEmpty() ? null : Path.of(session.transcript);
        AgentRun.Tokens total = transcript == null ? AgentRun.Tokens.NONE : tokens(transcript);
        List<Agent> agents = new ArrayList<>();
        for (Agent agent : session.agents.values()) {
            AgentRun.Tokens spent = transcript == null ? AgentRun.Tokens.NONE : tokens(subagent(transcript, agent.id()));
            total = total.plus(spent);
            agents.add(new Agent(agent.id(), agent.type(), agent.description(), agent.started(), agent.ended(), agent.action(), spent));
        }
        return new Session(session.id, session.tool, session.started, session.last, session.state, session.action, List.copyOf(agents),
                Map.copyOf(session.chapters), total);
    }

    /** Where Claude Code keeps the transcript of an agent of a session. */
    static Path subagent(Path transcript, String agent) {
        String name = transcript.getFileName().toString();
        String base = name.endsWith(".jsonl") ? name.substring(0, name.length() - ".jsonl".length()) : name;
        return transcript.resolveSibling(base).resolve("subagents").resolve("agent-" + agent + ".jsonl");
    }

    /** The tokens of a transcript: each message once. */
    static AgentRun.Tokens tokens(Path transcript) {
        if (!Files.isRegularFile(transcript)) {
            return AgentRun.Tokens.NONE;
        }
        AgentRun.Tokens tokens = AgentRun.Tokens.NONE;
        Set<String> counted = new HashSet<>();
        try (BufferedReader lines = Files.newBufferedReader(transcript, StandardCharsets.UTF_8)) {
            for (String line = lines.readLine(); line != null; line = lines.readLine()) {
                if (!line.contains("\"usage\"")) {
                    continue;
                }
                JsonNode message;
                try {
                    message = JSON.readTree(line).path("message");
                } catch (RuntimeException notJson) {
                    continue;
                }
                String id = message.path("id").asString("");
                if (!id.isEmpty() && counted.add(id)) {
                    tokens = tokens.plus(AgentRun.tokens(message.path("usage")));
                }
            }
        } catch (IOException e) {
            return tokens;
        }
        return tokens;
    }
}
