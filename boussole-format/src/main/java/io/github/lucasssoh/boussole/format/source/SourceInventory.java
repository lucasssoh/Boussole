package io.github.lucasssoh.boussole.format.source;

import io.github.lucasssoh.boussole.format.model.CourseSources;
import io.github.lucasssoh.boussole.format.model.SourceEntry;
import io.github.lucasssoh.boussole.format.model.SourceStatus;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Optional;

/**
 * Compares the documents of the sources folder, all its sub-folders included, with
 * {@code cursus/sources.yaml}: which are new, which changed since the course used them, which
 * wait to be taken in, which are gone, which are the same document in several places. The
 * validator reports from it, and {@code boussole sources} prints it for the agent.
 */
public final class SourceInventory {

    private SourceInventory() {
    }

    /** What is known of one document, or of one entry whose document is gone. */
    public record SourceState(String file, State state, Optional<String> sha256, Optional<SourceEntry> entry) {
    }

    public enum State {
        /** In the folder, in no entry. */
        NEW,
        /** In an entry with a SHA-256 the document no longer has. */
        CHANGED,
        /** In an entry with {@code status: pending}. */
        PENDING,
        USED,
        IGNORED,
        /** In an entry, no longer in the folder. */
        GONE;

        public boolean needsWork() {
            return this == NEW || this == CHANGED || this == PENDING;
        }
    }

    public static List<SourceState> compare(Optional<CourseSources> sources, SourceDocuments documents) {
        List<SourceEntry> entries = sources.map(CourseSources::entries).orElse(List.of());
        List<SourceState> states = new ArrayList<>();
        for (String file : documents.documents()) {
            Optional<SourceEntry> entry = entryFor(file, entries);
            states.add(new SourceState(file, state(file, entry, documents), documents.sha256(file), entry));
        }
        entries.stream()
                .filter(entry -> !entry.isPattern() && !documents.exists(entry.file()))
                .forEach(entry -> states.add(new SourceState(entry.file(), State.GONE, Optional.empty(), Optional.of(entry))));
        return List.copyOf(states);
    }

    /**
     * Documents that are byte for byte the same, in several places of the sources folder, and
     * that the course has not settled yet: more than one of them is not {@code ignored}.
     */
    public static List<List<String>> duplicates(Optional<CourseSources> sources, SourceDocuments documents) {
        List<SourceEntry> entries = sources.map(CourseSources::entries).orElse(List.of());
        Map<String, List<String>> bySha = new TreeMap<>();
        for (String file : documents.documents()) {
            documents.sha256(file).ifPresent(sha -> bySha.computeIfAbsent(sha, key -> new ArrayList<>()).add(file));
        }
        return bySha.values().stream()
                .filter(files -> files.size() > 1)
                .filter(files -> files.stream()
                        .filter(file -> entryFor(file, entries).map(SourceEntry::status).orElse(SourceStatus.USED) != SourceStatus.IGNORED)
                        .count() > 1)
                .map(List::copyOf)
                .toList();
    }

    /** The entry naming a file exactly comes first; else the first pattern matching it. */
    private static Optional<SourceEntry> entryFor(String file, List<SourceEntry> entries) {
        return entries.stream().filter(entry -> !entry.isPattern() && entry.file().equals(file)).findFirst()
                .or(() -> entries.stream().filter(entry -> entry.isPattern() && matcher(entry.file()).matches(Path.of(file))).findFirst());
    }

    private static State state(String file, Optional<SourceEntry> entry, SourceDocuments documents) {
        if (entry.isEmpty()) {
            return State.NEW;
        }
        SourceEntry found = entry.get();
        if (found.status() == SourceStatus.PENDING) {
            return State.PENDING;
        }
        if (!found.isPattern() && found.sha256().isPresent() && !found.sha256().equals(documents.sha256(file))) {
            return State.CHANGED;
        }
        return found.status() == SourceStatus.IGNORED ? State.IGNORED : State.USED;
    }

    private static PathMatcher matcher(String pattern) {
        return FileSystems.getDefault().getPathMatcher("glob:" + pattern);
    }
}
