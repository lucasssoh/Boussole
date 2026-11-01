package io.github.lucasssoh.boussole.cli.init;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class ClaudeHooksTest {

    @Test
    void theHooksOfTheBusJoinTheLearnersSettingsWithoutTouchingThem() {
        String learner = """
                {"model": "opus", "hooks": {"PreToolUse": [{"matcher": "Bash", "hooks": [{"type": "command", "command": "echo mine"}]}]}}
                """;

        String merged = ClaudeHooks.merge(Optional.of(learner));

        assertThat(merged).contains("\"model\" : \"opus\"", "echo mine");
        ClaudeHooks.EVENTS.forEach(event -> assertThat(merged).contains("\"" + event + "\""));
        assertThat(merged.split(ClaudeHooks.MARK, -1)).hasSize(ClaudeHooks.EVENTS.size() + 1);
    }

    @Test
    void installingTwiceChangesNothing() {
        String once = ClaudeHooks.merge(Optional.empty());

        assertThat(ClaudeHooks.merge(Optional.of(once))).isEqualTo(once);
    }
}
