package io.github.lucasssoh.boussole.format.diagnostic;

/** One problem found in a subject, printed as {@code file:line: code message}. */
public record Diagnostic(DiagnosticCode code, SourcePosition position, String message) {

    public DiagnosticSeverity severity() {
        return code.severity();
    }

    public boolean isError() {
        return severity() == DiagnosticSeverity.ERROR;
    }

    @Override
    public String toString() {
        return position + ": " + code + " " + message;
    }
}
