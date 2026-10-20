package io.github.lucasssoh.boussole.format.diagnostic;

/** One problem found in a subject, printed as {@code file:line: code message}. */
public record Diagnostic(Code code, Position position, String message) {

    public Severity severity() {
        return code.severity();
    }

    public boolean isError() {
        return severity() == Severity.ERROR;
    }

    @Override
    public String toString() {
        return position + ": " + code + " " + message;
    }
}
