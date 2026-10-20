package io.github.lucasssoh.boussole.core.learn;

import io.github.lucasssoh.boussole.core.learn.progress.ProgressRepository;
import io.github.lucasssoh.boussole.core.learn.progress.SessionKey;
import java.time.Clock;
import java.time.Duration;

/** Records that the learner reached the end of a session, and the time they actually spent on it. */
public final class FinishSession {

    private final CourseLookup lookup;
    private final ProgressRepository progress;
    private final Clock clock;

    public FinishSession(ContentRepository content, ProgressRepository progress, Clock clock) {
        this.lookup = new CourseLookup(content);
        this.progress = progress;
        this.clock = clock;
    }

    public void finish(SessionKey key, Duration spent) {
        lookup.session(lookup.course(key.subject()), key.session());
        progress.saveSessionFinished(key, clock.instant(), spent.isNegative() ? Duration.ZERO : spent);
    }
}
