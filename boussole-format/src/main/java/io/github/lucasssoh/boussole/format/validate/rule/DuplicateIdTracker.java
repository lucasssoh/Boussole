package io.github.lucasssoh.boussole.format.validate.rule;

import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCode;
import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCollector;
import io.github.lucasssoh.boussole.format.diagnostic.SourcePosition;
import java.util.HashMap;
import java.util.Map;

/** Remembers the IDs of one scope and reports the second use of any of them. */
final class DuplicateIdTracker {

    private final String what;
    private final DiagnosticCollector diagnostics;
    private final Map<String, SourcePosition> first = new HashMap<>();

    DuplicateIdTracker(String what, DiagnosticCollector diagnostics) {
        this.what = what;
        this.diagnostics = diagnostics;
    }

    void add(String id, SourcePosition position) {
        if (id.isBlank()) {
            return;
        }
        SourcePosition previous = first.putIfAbsent(id, position);
        if (previous != null) {
            diagnostics.report(DiagnosticCode.F003, position, what + " ID `" + id + "` is already used at " + previous);
        }
    }
}
