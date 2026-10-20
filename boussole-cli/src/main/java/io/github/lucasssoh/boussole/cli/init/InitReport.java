package io.github.lucasssoh.boussole.cli.init;

import java.util.ArrayList;
import java.util.List;

/** What {@code boussole init} did, one line per file or group of files. */
final class InitReport {

    enum Action {
        CREATED("Created"),
        UPDATED("Updated"),
        KEPT("Kept   "),
        WROTE("Wrote  ");

        private final String label;

        Action(String label) {
            this.label = label;
        }
    }

    private final List<String> lines = new ArrayList<>();

    void add(Action action, String what) {
        lines.add(action.label + "  " + what);
    }

    List<String> lines() {
        return List.copyOf(lines);
    }
}
