package io.github.lucasssoh.boussole.cli;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * {@code boussole bus EVENT}: an agent says what it does on a track, whatever the agent, so that
 * {@code boussole} shows it beside the plan. It adds a line to {@code .boussole/bus/events.jsonl}
 * of the track; Claude Code writes there by itself through hooks, other agents call this command,
 * as the {@code AGENTS.md} of the track tells them.
 */
@Command(
        name = "bus",
        mixinStandardHelpOptions = true,
        description = {
            "Tell Boussole what an agent does on a track, for the Tracks screen of boussole.",
            "EVENT is one of: start, doing, writing, agent-start, agent-end, waiting, end.",
            "Examples: boussole bus start --tool codex",
            "          boussole bus writing --tool codex --file cursus/01-turing/14-table.md",
            "          boussole bus agent-start --tool codex --agent ch2 --text \"Write chapter 2\""
        })
final class BusCommand implements Callable<Integer> {

    static final List<String> EVENTS = List.of("start", "doing", "writing", "agent-start", "agent-end", "waiting", "end");
    private static final ObjectMapper JSON = new ObjectMapper();

    @Parameters(index = "0", paramLabel = "EVENT", description = "start, doing, writing, agent-start, agent-end, waiting or end.")
    private String event;

    @Parameters(index = "1", paramLabel = "FOLDER", defaultValue = ".", description = "The track folder (default: here).")
    private Path folder;

    @Option(names = "--tool", paramLabel = "NAME", defaultValue = "agent", description = "The agent that works: codex, gemini, cursor…")
    private String tool;

    @Option(names = "--session", paramLabel = "ID", description = "Tells apart two sessions of the same tool (default: the tool).")
    private String session;

    @Option(names = "--agent", paramLabel = "ID", description = "An agent the session started (one per chapter), for its events.")
    private String agent;

    @Option(names = "--text", paramLabel = "TEXT", description = "What it does, in a few words; for agent-start, what it was asked.")
    private String text;

    @Option(names = "--file", paramLabel = "PATH", description = "With writing: the session file, cursus/<chapter>/<file>.md.")
    private String file;

    @Spec
    private CommandSpec spec;

    @Override
    public Integer call() {
        if (!EVENTS.contains(event)) {
            spec.commandLine().getErr().println("boussole bus: " + event + " is not an event; write one of " + String.join(", ", EVENTS));
            return ExitCode.UNUSABLE;
        }
        if (!Files.isRegularFile(folder.resolve("subject.yaml"))) {
            spec.commandLine().getErr().println("boussole bus: " + folder + " is not a track (no subject.yaml); run it in the track folder");
            return ExitCode.UNUSABLE;
        }
        ObjectNode line = JSON.createObjectNode().put("bus", 1).put("event", event).put("tool", tool)
                .put("session", session == null ? tool : session);
        if (agent != null) {
            line.put("agent", agent);
        }
        if (text != null) {
            line.put("text", text);
        }
        if (file != null) {
            line.put("file", file);
        }
        try {
            Path events = folder.resolve(".boussole").resolve("bus").resolve("events.jsonl");
            Files.createDirectories(events.getParent());
            Files.writeString(events, Instant.now().getEpochSecond() + " " + JSON.writeValueAsString(line) + "\n",
                    StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            spec.commandLine().getErr().println("boussole bus: " + e.getMessage());
            return ExitCode.UNUSABLE;
        }
        return ExitCode.OK;
    }
}
