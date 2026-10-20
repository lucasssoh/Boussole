package io.github.lucasssoh.boussole.core.learn;

import io.github.lucasssoh.boussole.core.learn.progress.ProgressRepository;
import io.github.lucasssoh.boussole.core.learn.progress.SessionKey;
import io.github.lucasssoh.boussole.core.learn.reward.RewardedSubjects;
import io.github.lucasssoh.boussole.core.learn.reward.XpRules;
import java.time.Clock;
import java.time.Duration;

/**
 * Records that the learner reached the end of a session, and the time they actually spent on it;
 * returns the experience it earned.
 */
public final class FinishSession {

    private final CourseLookup lookup;
    private final ProgressRepository progress;
    private final RewardedSubjects rewarded;
    private final Clock clock;

    public FinishSession(ContentRepository content, ProgressRepository progress, RewardedSubjects rewarded, Clock clock) {
        this.lookup = new CourseLookup(content);
        this.progress = progress;
        this.rewarded = rewarded;
        this.clock = clock;
    }

    public int finish(SessionKey key, Duration spent) {
        lookup.session(lookup.course(key.subject()), key.session());
        progress.saveSessionFinished(key, clock.instant(), spent.isNegative() ? Duration.ZERO : spent);
        return rewarded.rewards(key.subject()) ? XpRules.SESSION_FINISHED : 0;
    }
}
