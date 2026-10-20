package io.github.lucasssoh.boussole.format.validate.rule;

import io.github.lucasssoh.boussole.format.diagnostic.Code;
import io.github.lucasssoh.boussole.format.diagnostic.Diagnostics;
import io.github.lucasssoh.boussole.format.diagnostic.Position;
import java.util.HashMap;
import java.util.Map;

/** Remembers the IDs of one scope and reports the second use of any of them. */
final class IdSeen {

    private final String what;
    private final Diagnostics diagnostics;
    private final Map<String, Position> first = new HashMap<>();

    IdSeen(String what, Diagnostics diagnostics) {
        this.what = what;
        this.diagnostics = diagnostics;
    }

    void add(String id, Position position) {
        if (id.isBlank()) {
            return;
        }
        Position previous = first.putIfAbsent(id, position);
        if (previous != null) {
            diagnostics.report(Code.F003, position, what + " ID `" + id + "` is already used at " + previous);
        }
    }
}
