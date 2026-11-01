package io.github.lucasssoh.boussole.cli.admin;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Approves the plan an agent proposed: {@code status: proposed} becomes {@code status: approved}
 * in {@code cursus/syllabus.yaml}, the rest of the file as it was, comments included. The agents
 * of the track then write its sessions, as the {@code AGENTS.md} of the track says.
 */
final class PlanApproval {

    private static final Pattern PROPOSED = Pattern.compile("^status:[ \\t]*[\"']?proposed[\"']?[ \\t]*(#.*)?$",
            Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);

    private PlanApproval() {
    }

    static void approve(Path folder) {
        Path syllabus = folder.resolve("cursus").resolve("syllabus.yaml");
        try {
            String text = Files.readString(syllabus, StandardCharsets.UTF_8);
            Matcher found = PROPOSED.matcher(text);
            if (!found.find()) {
                throw new IllegalStateException("No proposed plan in " + Painter.folder(syllabus));
            }
            String comment = found.group(1) == null ? "" : " " + found.group(1);
            String approved = text.substring(0, found.start()) + "status: approved" + comment + text.substring(found.end());
            Files.writeString(syllabus, approved, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
