package io.github.lucasssoh.boussole.format.diagnostic;

import static io.github.lucasssoh.boussole.format.diagnostic.DiagnosticSeverity.ERROR;
import static io.github.lucasssoh.boussole.format.diagnostic.DiagnosticSeverity.WARNING;

/**
 * The diagnostic codes of {@code docs/FORMAT.md} §6. {@code F} codes are about the format,
 * {@code P} codes about the teaching contract.
 */
public enum DiagnosticCode {
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
    F022(ERROR, "MCQ choices inconsistent"),
    F023(ERROR, "fill gaps do not match"),
    F024(ERROR, "numeric answer is not a number"),
    F025(ERROR, "invalid regular expression"),
    F026(ERROR, "container not closed, or closing line without container"),
    F027(ERROR, "attribute unknown, missing or invalid"),
    F030(ERROR, "reference to an unknown ID"),
    F031(ERROR, "source file or page not found"),
    F032(WARNING, "video unknown to YouTube"),
    F033(WARNING, "document not yet taken into the course"),
    F034(WARNING, "document changed since the course used it"),
    F035(WARNING, "document listed but gone"),
    F036(WARNING, "same document in several places"),
    F037(ERROR, "graph malformed"),
    F038(WARNING, "video file a browser may not play"),
    F039(WARNING, "document from the web without its licence"),
    F040(ERROR, "versions of a track differ"),
    F041(WARNING, "session written in another version, not yet in this one"),
    F042(ERROR, "question asked again before it is taught"),
    P001(WARNING, "notion without exercise"),
    P002(WARNING, "notion with several examples"),
    P003(WARNING, "warmup not first, or practice not last"),
    P004(WARNING, "question without hint"),
    P005(WARNING, "term used but not declared"),
    P006(WARNING, "session does not close as the learner asked"),
    P007(WARNING, "notion without the quick check the learner asked for"),
    P008(WARNING, "timed session whose questions do not fit its time"),
    P009(WARNING, "timed session with a notion or a worked example"),
    P010(WARNING, "MCQ wrong choice without feedback"),
    P011(WARNING, "graph too large to read at a glance"),
    P012(WARNING, "session that does not say who wrote it");

    private final DiagnosticSeverity severity;
    private final String summary;

    DiagnosticCode(DiagnosticSeverity severity, String summary) {
        this.severity = severity;
        this.summary = summary;
    }

    public DiagnosticSeverity severity() {
        return severity;
    }

    public String summary() {
        return summary;
    }
}
