package io.github.lucasssoh.boussole.cli.init;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * The agent kit packaged with Boussole: the skills of the repository's {@code skills/}, its
 * {@code docs/FORMAT.md}, and the {@code AGENTS.md} block. Copied into the jar at build time.
 */
final class AgentKit {

    /** Every skill of {@code skills/}; a test keeps this list in step with the folder. */
    static final List<String> SKILLS =
            List.of("learner-profile", "cursus-discover", "cursus-plan", "seance-write", "exercise-author", "exam-build", "lexique-build", "video-find", "sources-update", "cursus-fix");

    private static final String ROOT = "/boussole-agent/";

    private AgentKit() {
    }

    static String skill(String name) {
        return resource("skills/" + name + "/SKILL.md");
    }

    static String format() {
        return resource("FORMAT.md");
    }

    static String agentsBlock(String sources, String validateCommand) {
        String where = sources.equals(".")
                ? "this folder: every file outside `cursus/` and `.boussole/`"
                : "`" + sources + "/`";
        return resource("AGENTS.md").replace("{{sources}}", where).replace("{{validate}}", validateCommand);
    }

    private static String resource(String path) {
        try (InputStream stream = AgentKit.class.getResourceAsStream(ROOT + path)) {
            if (stream == null) {
                throw new IllegalStateException("missing from the build: " + ROOT + path);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
