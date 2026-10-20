package io.github.lucasssoh.boussole.cli.init;

import io.github.lucasssoh.boussole.adapters.config.BoussolePaths;
import java.nio.file.Path;

/** Where the learner keeps the profile reused by every new subject. */
final class LearnerProfiles {

    private LearnerProfiles() {
    }

    /** {@code $XDG_CONFIG_HOME/boussole/learner.yaml}, or {@code ~/.config/boussole/learner.yaml}. */
    static Path usual() {
        return BoussolePaths.forCurrentUser().learnerProfile();
    }
}
