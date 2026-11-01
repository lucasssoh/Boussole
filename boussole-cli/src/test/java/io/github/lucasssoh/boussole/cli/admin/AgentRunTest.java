package io.github.lucasssoh.boussole.cli.admin;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AgentRunTest {

    @TempDir
    Path dir;

    /** Two parts of one message, a second message, then the result with the totals of every model. */
    static final String EVENTS = """
            {"type":"system","subtype":"init"}
            {"type":"assistant","message":{"id":"m1","content":[{"type":"text","text":"Je lis le plan."}],"usage":{"input_tokens":10,"output_tokens":5,"cache_read_input_tokens":100,"cache_creation_input_tokens":50}}}
            {"type":"assistant","message":{"id":"m1","content":[{"type":"tool_use","name":"Read","input":{"file_path":"cursus/syllabus.yaml"}}],"usage":{"input_tokens":10,"output_tokens":5,"cache_read_input_tokens":100,"cache_creation_input_tokens":50}}}
            {"type":"assistant","message":{"id":"m2","content":[{"type":"tool_use","name":"Agent","input":{"description":"Write chapter 1"}}],"usage":{"input_tokens":1,"output_tokens":2,"cache_read_input_tokens":3,"cache_creation_input_tokens":4}}}
            {"type":"result","subtype":"success","is_error":false,"duration_ms":125000,"total_cost_usd":1.5,"result":"Sessions written.","modelUsage":{"claude-opus-5-5":{"inputTokens":1000,"outputTokens":2000,"cacheReadInputTokens":3000,"cacheCreationInputTokens":4000}}}
            """;

    @Test
    void aFinishedRunGivesTheTokensOfEveryModelAndItsCost() throws Exception {
        Path log = Files.writeString(dir.resolve("20261101-120000.jsonl"), EVENTS);

        AgentRun.Status status = AgentRun.read(log);

        assertThat(status.state()).isEqualTo(AgentRun.State.DONE);
        assertThat(status.tokens()).isEqualTo(new AgentRun.Tokens(1000, 2000, 3000, 4000));
        assertThat(status.costUsd()).contains(1.5);
        assertThat(status.result()).isEqualTo("Sessions written.");
    }

    @Test
    void aRunWhoseChapterAgentsWereCutIsStopped() throws Exception {
        Path log = Files.writeString(dir.resolve("20261101-120000.jsonl"), EVENTS);
        Files.writeString(dir.resolve("20261101-120000.err"), "Background tasks still running 10m after the last turn; stopping them.\n");

        assertThat(AgentRun.read(log).state()).isEqualTo(AgentRun.State.STOPPED);
    }

    @Test
    void aRunWithoutItsResultCountsEachMessageOnce() throws Exception {
        String unfinished = EVENTS.lines().filter(line -> !line.contains("\"result\"")).reduce("", (a, b) -> a + b + "\n");
        Path log = Files.writeString(dir.resolve("20261101-120000.jsonl"), unfinished);

        AgentRun.Status status = AgentRun.read(log);

        assertThat(status.state()).isEqualTo(AgentRun.State.STOPPED);
        assertThat(status.tokens()).isEqualTo(new AgentRun.Tokens(11, 7, 103, 54));
        assertThat(status.recent()).containsExactly("Je lis le plan.", "Read cursus/syllabus.yaml", "Agent Write chapter 1");
    }

    @Test
    void theAgentMayChangeFilesRunTheValidatorReadTheDocumentsAndComputeAnswers() {
        List<String> command = AgentRun.command("claude", "/opt/boussole.jar");

        assertThat(command).startsWith("claude", "--print").contains("acceptEdits", "Bash(boussole validate:*)",
                "Bash(java -jar /opt/boussole.jar index:*)", "Bash(pdftotext:*)", "Bash(python3:*)").doesNotContain("Bash", "WebFetch");
    }

    @Test
    void eachChapterGivesTheSessionItsAgentWritesAndItsLastAction() throws Exception {
        String events = """
                {"type":"assistant","message":{"id":"a1","content":[{"type":"tool_use","name":"Agent","input":{"description":"Write chapter 1"}}]}}
                {"type":"assistant","parent_tool_use_id":"t1","message":{"id":"b1","content":[{"type":"tool_use","name":"Write","input":{"file_path":"/c/L&MC/cursus/01-turing/11-mots.md"}}]}}
                {"type":"assistant","parent_tool_use_id":"t1","message":{"id":"b2","content":[{"type":"tool_use","name":"Write","input":{"file_path":"/c/L&MC/cursus/01-turing/12-formaliser.md"}}]}}
                {"type":"assistant","parent_tool_use_id":"t1","message":{"id":"b3","content":[{"type":"tool_use","name":"Bash","input":{"description":"Validate the course"}}]}}
                {"type":"assistant","parent_tool_use_id":"t3","message":{"id":"c1","content":[{"type":"tool_use","name":"Edit","input":{"file_path":"/c/L&MC/cursus/03-lambda/31-termes.md"}}]}}
                """;
        Path log = Files.writeString(dir.resolve("20261101-120000.jsonl"), events);

        AgentRun.Status status = AgentRun.read(log);

        assertThat(status.chapter("01-turing")).contains(new AgentRun.Chapter("12-formaliser.md", "Bash Validate the course"));
        assertThat(status.chapter("03-lambda")).contains(new AgentRun.Chapter("31-termes.md", "Edit /c/L&MC/cursus/03-lambda/31-termes.md"));
        assertThat(status.chapter("02-other")).isEmpty();
    }

    @Test
    void theAgentsOfTheChaptersAreFollowedFromTheirStartToTheirEnd() throws Exception {
        String events = """
                {"type":"system","subtype":"task_started","task_id":"a2c39a44e2266ebbe","tool_use_id":"t1","description":"Write chapter 01 sessions","spawn_depth":1}
                {"type":"system","subtype":"background_tasks_changed","tasks":[{"task_id":"a2c39a44e2266ebbe"}]}
                {"type":"system","subtype":"task_started","task_id":"a28b0c8283d4291b6","tool_use_id":"t3","description":"Write chapter 03 sessions","spawn_depth":1}
                {"type":"system","subtype":"background_tasks_changed","tasks":[{"task_id":"a2c39a44e2266ebbe"},{"task_id":"a28b0c8283d4291b6"}]}
                {"type":"system","subtype":"task_progress","task_id":"a2c39a44e2266ebbe","description":"Reading .boussole/FORMAT.md","usage":{"total_tokens":20581,"tool_uses":2,"duration_ms":125000}}
                {"type":"assistant","parent_tool_use_id":"t1","message":{"id":"b1","content":[{"type":"tool_use","id":"u1","name":"Bash","input":{"command":"cd \\"/c/L&MC\\" && pdftotext -layout MdeTuring.pdf -"}}]}}
                {"type":"system","subtype":"permission_denied","tool_use_id":"u1","tool_name":"Bash"}
                {"type":"system","subtype":"background_tasks_changed","tasks":[{"task_id":"a2c39a44e2266ebbe"}]}
                {"type":"rate_limit_event","rate_limit_info":{"status":"rejected","resetsAt":1793548800,"rateLimitType":"five_hour","unifiedWindows":{"five_hour":{"utilization":1.04}}}}
                {"type":"system","subtype":"task_updated","task_id":"a2c39a44e2266ebbe","patch":{"status":"failed","error":"Agent terminated early due to an API error: You've hit your 5-hour limit"}}
                {"type":"system","subtype":"task_notification","task_id":"a2c39a44e2266ebbe","status":"failed"}
                {"type":"system","subtype":"background_tasks_changed","tasks":[]}
                {"type":"result","subtype":"success","is_error":false,"result":"You've hit your limit"}
                """;
        Path log = Files.writeString(dir.resolve("20261101-120000.jsonl"), events);

        AgentRun.Status status = AgentRun.read(log);

        assertThat(status.tasks()).extracting(AgentRun.Task::id, AgentRun.Task::state, AgentRun.Task::tools)
                .containsExactly(org.assertj.core.groups.Tuple.tuple("a2c39a44e2266ebbe", AgentRun.TaskState.FAILED, 2),
                        org.assertj.core.groups.Tuple.tuple("a28b0c8283d4291b6", AgentRun.TaskState.DONE, 0));
        assertThat(status.tasks().getFirst().action()).isEqualTo("Agent terminated early due to an API error: You've hit your 5-hour limit");
        assertThat(status.refused()).containsExactly("pdftotext");
        assertThat(status.limit()).hasValueSatisfying(limit -> assertThat(limit.reached()).isTrue());
        assertThat(status.state()).isEqualTo(AgentRun.State.STOPPED);
    }

    @Test
    void theNotificationComesAfterTheAgentWithTheNameOfTheTrack() {
        List<String> command = AgentRun.notifying("L&MC", "/opt/boussole.jar", List.of("claude", "--print"));

        assertThat(command).startsWith("sh", "-c").endsWith("boussole-agent", "L&MC", "/opt/boussole.jar", "claude", "--print");
        assertThat(command.get(2)).contains("notify-send", "validate .");
    }

    @Test
    void tokensReadShort() {
        assertThat(AgentRun.count(512)).isEqualTo("512");
        assertThat(AgentRun.count(34_567)).isEqualTo("34k");
        assertThat(AgentRun.count(14_200_000)).isEqualTo("14.2M");
    }
}
