package io.github.lucasssoh.boussole.cli.admin;

import io.github.lucasssoh.boussole.format.source.SourceInventory.SourceState;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * The documents of a track the learner ticks before {@code r} takes them in: only those, the
 * others wait. Nothing is ticked at first, so that nothing costs tokens unasked.
 */
final class DocumentPick {

    private final Path track;
    private final List<SourceState> documents;
    private final Set<String> ticked = new LinkedHashSet<>();
    private int cursor;

    DocumentPick(Path track, List<SourceState> documents) {
        this.track = track;
        this.documents = List.copyOf(documents);
    }

    Path track() {
        return track;
    }

    List<SourceState> documents() {
        return documents;
    }

    int cursor() {
        return cursor;
    }

    void move(int by) {
        cursor = Math.clamp(cursor + by, 0, Math.max(0, documents.size() - 1));
    }

    /** Ticks the document under the cursor, or unticks it. */
    void toggle() {
        if (documents.isEmpty()) {
            return;
        }
        String file = documents.get(cursor).file();
        if (!ticked.remove(file)) {
            ticked.add(file);
        }
    }

    /** Ticks every document, or none once they all are. */
    void toggleAll() {
        if (ticked.size() == documents.size()) {
            ticked.clear();
        } else {
            documents.forEach(document -> ticked.add(document.file()));
        }
    }

    boolean ticked(SourceState document) {
        return ticked.contains(document.file());
    }

    /** The ticked documents, in the order of the list. */
    Set<String> chosen() {
        Set<String> chosen = new LinkedHashSet<>();
        documents.stream().map(SourceState::file).filter(ticked::contains).forEach(chosen::add);
        return chosen;
    }
}
