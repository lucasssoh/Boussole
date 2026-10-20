package io.github.lucasssoh.boussole.cli.init;

import java.nio.file.Path;
import java.util.Optional;

/** Where the learner keeps the profile reused by every new subject. */
final class LearnerProfiles {

    private LearnerProfiles() {
    }

    /** {@code $XDG_CONFIG_HOME/boussole/learner.yaml}, or {@code ~/.config/boussole/learner.yaml}. */
    static Path usual() {
        Path config = Optional.ofNullable(System.getenv("XDG_CONFIG_HOME"))
                .filter(value -> !value.isBlank())
                .map(Path::of)
                .orElseGet(() -> Path.of(System.getProperty("user.home"), ".config"));
        return config.resolve("boussole").resolve("learner.yaml");
    }
}
