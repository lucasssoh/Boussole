package io.github.lucasssoh.boussole.cli.admin;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AgentBusTest {

    @TempDir
    Path track;

    private static final long NOW = 1793552000L;

    @Test
    void anyAgentThatCallsBoussoleBusIsFollowedWithItsAgentsAndTheSessionTheyWrite() throws Exception {
        write(NOW + " {\"bus\":1,\"event\":\"start\",\"tool\":\"codex\",\"session\":\"codex\"}",
                NOW + 5 + " {\"bus\":1,\"event\":\"agent-start\",\"tool\":\"codex\",\"session\":\"codex\",\"agent\":\"ch1\",\"text\":\"Write chapter 1\"}",
                NOW + 9 + " {\"bus\":1,\"event\":\"writing\",\"tool\":\"codex\",\"session\":\"codex\",\"agent\":\"ch1\",\"file\":\"cursus/01-turing/14-table.md\"}");

        List<AgentBus.Session> sessions = AgentBus.sessions(track, Instant.ofEpochSecond(NOW + 60));

        assertThat(sessions).singleElement().satisfies(session -> {
            assertThat(session.tool()).isEqualTo("codex");
            assertThat(session.state()).isEqualTo(AgentBus.SessionState.WORKING);
            assertThat(session.agents()).singleElement().satisfies(agent -> {
                assertThat(agent.description()).isEqualTo("Write chapter 1");
                assertThat(agent.action()).isEqualTo("Writing cursus/01-turing/14-table.md");
                assertThat(agent.ended()).isEmpty();
            });
            assertThat(session.chapters().get("01-turing").file()).isEqualTo("14-table.md");
        });
    }

    @Test
    void claudeCodeIsFollowedThroughItsHooksWithTheTokensOfItsTranscripts() throws Exception {
        Path transcript = track.resolve("s1.jsonl");
        Files.writeString(transcript, "{\"type\":\"assistant\",\"message\":{\"id\":\"m1\",\"usage\":{\"input_tokens\":1,\"output_tokens\":2}}}\n");
        Files.createDirectories(track.resolve("s1/subagents"));
        Files.writeString(track.resolve("s1/subagents/agent-a8a075bf.jsonl"),
                "{\"type\":\"assistant\",\"message\":{\"id\":\"m2\",\"usage\":{\"input_tokens\":10,\"output_tokens\":20}}}\n");
        String common = "\"session_id\":\"s1\",\"transcript_path\":\"" + transcript + "\"";
        write(NOW + " {\"hook_event_name\":\"SessionStart\"," + common + "}",
                NOW + 2 + " {\"hook_event_name\":\"PreToolUse\"," + common + ",\"tool_name\":\"Agent\",\"tool_input\":{\"description\":\"Count words\"}}",
                NOW + 3 + " {\"hook_event_name\":\"SubagentStart\"," + common + ",\"agent_id\":\"a8a075bf\",\"agent_type\":\"general-purpose\"}",
                NOW + 4 + " {\"hook_event_name\":\"PreToolUse\"," + common + ",\"agent_id\":\"a8a075bf\",\"tool_name\":\"Read\",\"tool_input\":{\"file_path\":\"note.txt\"}}",
                NOW + 6 + " {\"hook_event_name\":\"SubagentStop\"," + common + ",\"agent_id\":\"a8a075bf\"}",
                NOW + 7 + " {\"hook_event_name\":\"Stop\"," + common + ",\"background_tasks\":[]}");

        List<AgentBus.Session> sessions = AgentBus.sessions(track, Instant.ofEpochSecond(NOW + 60));

        assertThat(sessions).singleElement().satisfies(session -> {
            assertThat(session.tool()).isEqualTo("Claude Code");
            assertThat(session.state()).isEqualTo(AgentBus.SessionState.WAITING);
            assertThat(session.tokens().total()).isEqualTo(33);
            assertThat(session.agents()).singleElement().satisfies(agent -> {
                assertThat(agent.type()).isEqualTo("general-purpose");
                assertThat(agent.description()).isEqualTo("Count words");
                assertThat(agent.action()).isEqualTo("Read note.txt");
                assertThat(agent.ended()).isPresent();
                assertThat(agent.tokens().total()).isEqualTo(30);
            });
        });
    }

    @Test
    void aSessionWithoutNewsForLongEndsAndThenLeavesTheScreen() throws Exception {
        write(NOW + " {\"bus\":1,\"event\":\"start\",\"tool\":\"gemini\",\"session\":\"gemini\"}");

        assertThat(AgentBus.sessions(track, Instant.ofEpochSecond(NOW).plus(AgentBus.GONE).plusSeconds(60)))
                .singleElement().extracting(AgentBus.Session::state).isEqualTo(AgentBus.SessionState.ENDED);
        assertThat(AgentBus.sessions(track, Instant.ofEpochSecond(NOW).plus(AgentBus.GONE).plus(AgentBus.SHOWN).plusSeconds(60))).isEmpty();
    }

    private void write(String... lines) throws Exception {
        Path events = AgentBus.events(track);
        Files.createDirectories(events.getParent());
        Files.writeString(events, String.join("\n", lines) + "\n");
    }
}
