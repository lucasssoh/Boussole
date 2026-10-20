package io.github.lucasssoh.boussole.format.diagnostic;

import java.util.ArrayList;
import java.util.List;

/** Collects the diagnostics of one inspection, in the order they are found. */
public final class DiagnosticCollector {

    private final List<Diagnostic> found = new ArrayList<>();

    public void report(DiagnosticCode code, SourcePosition position, String message) {
        found.add(new Diagnostic(code, position, message));
    }

    public void addAll(DiagnosticCollector other) {
        found.addAll(other.found);
    }

    public List<Diagnostic> list() {
        return List.copyOf(found);
    }
}
