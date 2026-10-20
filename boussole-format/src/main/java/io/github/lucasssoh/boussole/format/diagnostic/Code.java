package io.github.lucasssoh.boussole.format.diagnostic;

import static io.github.lucasssoh.boussole.format.diagnostic.Severity.ERROR;
import static io.github.lucasssoh.boussole.format.diagnostic.Severity.WARNING;

/**
 * The diagnostic codes of {@code docs/FORMAT.md} §6. {@code F} codes are about the format,
 * {@code P} codes about the teaching contract.
 */
public enum Code {
    F001(ERROR, "YAML file unreadable, or a required field missing or invalid"),
    F002(ERROR, "unknown format version"),
    F003(ERROR, "duplicate ID"),
    F004(ERROR, "invalid ID"),
    F010(ERROR, "session ID does not match the syllabus"),
    F011(WARNING, "session file not written yet"),
    F012(ERROR, "level-1 heading in a session"),
    F013(ERROR, "level-2 heading without an ID"),
    F020(ERROR, "unknown or misplaced directive"),
    F021(ERROR, "exercise parts missing, repeated or malformed"),
    F022(ERROR, "qcm choices inconsistent"),
    F023(ERROR, "fill gaps do not match"),
    F024(ERROR, "numeric answer is not a number"),
    F025(ERROR, "invalid regular expression"),
    F026(ERROR, "container not closed, or closing line without container"),
    F027(ERROR, "attribute unknown, missing or invalid"),
    F030(ERROR, "reference to an unknown ID"),
    F031(ERROR, "source file or page not found"),
    F032(WARNING, "video unknown to YouTube"),
    P001(WARNING, "notion without exercise"),
    P002(WARNING, "notion with several examples"),
    P003(WARNING, "warmup not first, or practice not last"),
    P004(WARNING, "exercise without hint"),
    P005(WARNING, "term used but not declared");

    private final Severity severity;
    private final String summary;

    Code(Severity severity, String summary) {
        this.severity = severity;
        this.summary = summary;
    }

    public Severity severity() {
        return severity;
    }

    public String summary() {
        return summary;
    }
}
