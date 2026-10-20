package io.github.lucasssoh.boussole.cli.init;

import io.github.lucasssoh.boussole.cli.init.InitReport.Action;

/**
 * Installs the agent kit in a subject folder: {@code .boussole/} is Boussole's and rewritten each
 * time; {@code AGENTS.md} and {@code CLAUDE.md} may be the learner's, so only Boussole's block in
 * them is touched.
 */
final class AgentKitInstaller {

    static final String KIT = ".boussole/";
    static final String AGENTS = "AGENTS.md";
    static final String CLAUDE = "CLAUDE.md";
    private static final String CLAUDE_IMPORT = "@AGENTS.md";

    private final SubjectFolder folder;

    AgentKitInstaller(SubjectFolder folder) {
        this.folder = folder;
    }

    void install(String sources, String validateCommand, InitReport report) {
        folder.write(KIT + "FORMAT.md", AgentKit.format());
        AgentKit.SKILLS.forEach(skill -> folder.write(KIT + "skills/" + skill + "/SKILL.md", AgentKit.skill(skill)));
        report.add(Action.WROTE, KIT + "FORMAT.md and " + AgentKit.SKILLS.size() + " skills in " + KIT + "skills/");

        boolean hadAgents = folder.exists(AGENTS);
        folder.write(AGENTS, MarkedBlock.upsert(folder.read(AGENTS), AgentKit.agentsBlock(sources, validateCommand)));
        report.add(hadAgents ? Action.UPDATED : Action.CREATED, AGENTS);

        // Claude Code reads CLAUDE.md, not AGENTS.md: an import line makes it read both.
        String claude = folder.read(CLAUDE).orElse("");
        if (claude.lines().anyMatch(line -> line.strip().equals(CLAUDE_IMPORT))) {
            report.add(Action.KEPT, CLAUDE);
        } else {
            folder.write(CLAUDE, claude.isEmpty() ? CLAUDE_IMPORT + "\n" : CLAUDE_IMPORT + "\n\n" + claude);
            report.add(claude.isEmpty() ? Action.CREATED : Action.UPDATED, CLAUDE);
        }
    }
}
