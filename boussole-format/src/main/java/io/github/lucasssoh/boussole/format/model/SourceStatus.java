package io.github.lucasssoh.boussole.format.model;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/** What the course did with a document of the sources folder. */
public enum SourceStatus {
    /** Taken into the course: its content is in the syllabus and the sessions. */
    USED,
    /** Deliberately left out: a duplicate, an administrative document, an old version. */
    IGNORED,
    /** Seen, and still to be taken into the course. */
    PENDING;

    public String written() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static Optional<SourceStatus> fromWritten(String value) {
        return Arrays.stream(values()).filter(status -> status.written().equals(value)).findFirst();
    }
}
