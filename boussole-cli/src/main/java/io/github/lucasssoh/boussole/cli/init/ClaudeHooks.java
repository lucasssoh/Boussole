package io.github.lucasssoh.boussole.cli.init;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * The bus of a track: hooks of Claude Code, in {@code .claude/settings.json} of the track, that
 * add a line to {@code .boussole/bus/events.jsonl} each time a session in the folder starts,
 * acts, starts or ends an agent, waits or ends. Any session counts, the one {@code boussole}
 * starts as well as one the learner opens, and {@code boussole} follows them all from there. A
 * line is the time in seconds, a space, then what Claude Code gives the hook, on one line. The
 * hooks are shell, not Java, so that each action of an agent costs a few milliseconds.
 */
public final class ClaudeHooks {

    static final String SETTINGS = ".claude/settings.json";
    /** Marks Boussole's hooks among the learner's, so that they are replaced and never doubled. */
    static final String MARK = "boussole-bus";
    static final String COMMAND = "d=\"$CLAUDE_PROJECT_DIR/.boussole/bus\"; mkdir -p \"$d\" && { printf '%s ' \"$(date +%s)\";"
            + " tr -d '\\n'; echo; } >> \"$d/events.jsonl\"; exit 0 # " + MARK;
    static final List<String> EVENTS = List.of("SessionStart", "PreToolUse", "SubagentStart", "SubagentStop", "Stop", "SessionEnd");

    private static final ObjectMapper JSON = JsonMapper.builder().enable(SerializationFeature.INDENT_OUTPUT).build();

    private ClaudeHooks() {
    }

    /** Writes Boussole's hooks into the settings of the track; returns true when they changed. */
    public static boolean install(Path track) {
        Path settings = track.resolve(SETTINGS);
        try {
            Optional<String> before = Files.isRegularFile(settings)
                    ? Optional.of(Files.readString(settings, StandardCharsets.UTF_8)) : Optional.empty();
            String after = merge(before);
            if (before.map(after::equals).orElse(false)) {
                return false;
            }
            Files.createDirectories(settings.getParent());
            Files.writeString(settings, after, StandardCharsets.UTF_8);
            return true;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** The settings with Boussole's hooks, everything else as it was. */
    static String merge(Optional<String> settings) {
        return merge(settings, Sandbox.problem().isEmpty());
    }

    /**
     * The same; with {@code sandbox}, every command of an agent runs in the sandbox and is allowed
     * (see {@link Sandbox}), unless the learner set the sandbox up their own way.
     */
    static String merge(Optional<String> settings, boolean sandbox) {
        JsonNode read = settings.filter(text -> !text.isBlank()).map(JSON::readTree).orElse(null);
        ObjectNode root = read instanceof ObjectNode object ? object : JSON.createObjectNode();
        ObjectNode hooks = root.get("hooks") instanceof ObjectNode existing ? existing : root.putObject("hooks");
        for (String event : EVENTS) {
            ArrayNode groups = hooks.get(event) instanceof ArrayNode existing ? existing : hooks.putArray(event);
            for (int index = groups.size() - 1; index >= 0; index--) {
                if (groups.get(index).toString().contains(MARK)) {
                    groups.remove(index);
                }
            }
            ObjectNode group = groups.addObject();
            if (event.equals("PreToolUse")) {
                group.put("matcher", "*");
            }
            group.putArray("hooks").addObject().put("type", "command").put("command", COMMAND);
        }
        if (sandbox && !root.has("sandbox")) {
            root.set("sandbox", JSON.readTree(Sandbox.SETTINGS).get("sandbox"));
            ObjectNode permissions = root.get("permissions") instanceof ObjectNode existing ? existing : root.putObject("permissions");
            ArrayNode allow = permissions.get("allow") instanceof ArrayNode existing ? existing : permissions.putArray("allow");
            boolean bash = false;
            for (JsonNode rule : allow) {
                bash |= rule.asString("").equals("Bash");
            }
            if (!bash) {
                allow.add("Bash");
            }
        }
        return JSON.writeValueAsString(root) + "\n";
    }
}
