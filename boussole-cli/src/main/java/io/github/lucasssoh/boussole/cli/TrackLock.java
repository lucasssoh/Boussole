package io.github.lucasssoh.boussole.cli;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Locks a track, or unlocks it: the {@code locked} line of {@code cursus/syllabus.yaml}, written
 * after {@code status}, the rest of the file as it was. Only the learner does it, with
 * {@code boussole lock} or {@code L} in {@code boussole}.
 */
public final class TrackLock {

    private static final Pattern LOCKED = Pattern.compile("^locked:.*(\\R|\\z)", Pattern.MULTILINE);
    private static final Pattern STATUS = Pattern.compile("^status:.*$", Pattern.MULTILINE);

    private TrackLock() {
    }

    public static void lock(Path folder, LocalDate day) {
        String text = unlocked(read(folder));
        Matcher status = STATUS.matcher(text);
        String line = "locked: " + day;
        String locked = status.find()
                ? text.substring(0, status.end()) + "\n" + line + text.substring(status.end())
                : line + "\n" + text;
        write(folder, locked);
    }

    public static void unlock(Path folder) {
        write(folder, unlocked(read(folder)));
    }

    private static String unlocked(String text) {
        return LOCKED.matcher(text).replaceAll("");
    }

    private static Path syllabus(Path folder) {
        return folder.resolve("cursus").resolve("syllabus.yaml");
    }

    private static String read(Path folder) {
        try {
            return Files.readString(syllabus(folder), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("No plan to lock in " + folder + ": " + e.getMessage(), e);
        }
    }

    private static void write(Path folder, String text) {
        try {
            Files.writeString(syllabus(folder), text, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
