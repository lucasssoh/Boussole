package io.github.lucasssoh.boussole.core.learn.reward;

import io.github.lucasssoh.boussole.core.learn.progress.ProgressRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/** Experience, level and streak, computed from the history of attempts and finished sessions. */
public final class ReadStudyStats {

    private final ProgressRepository progress;
    private final Clock clock;

    public ReadStudyStats(ProgressRepository progress, Clock clock) {
        this.progress = progress;
        this.clock = clock;
    }

    public StudyStats read() {
        List<AttemptRecord> attempts = progress.attemptHistory();
        List<Instant> finished = progress.sessionsFinishedAt();
        int xp = xpOf(attempts) + finished.size() * XpRules.SESSION_FINISHED;
        Set<LocalDate> days = new TreeSet<>();
        attempts.forEach(attempt -> days.add(day(attempt.at())));
        LocalDate today = LocalDate.now(clock);
        return new StudyStats(xp, Level.of(xp), streak(days, today), days.contains(today));
    }

    private static int xpOf(List<AttemptRecord> attempts) {
        Set<String> answered = new HashSet<>();
        return attempts.stream()
                .mapToInt(attempt -> XpRules.forAttempt(answered.add(attempt.subject() + "/" + attempt.exercise()), attempt.verdict()))
                .sum();
    }

    /** Days in a row ending today, or yesterday while today has nothing yet. */
    private static int streak(Set<LocalDate> days, LocalDate today) {
        LocalDate day = days.contains(today) ? today : today.minusDays(1);
        int count = 0;
        while (days.contains(day)) {
            count++;
            day = day.minusDays(1);
        }
        return count;
    }

    private LocalDate day(Instant at) {
        return at.atZone(clock.getZone()).toLocalDate();
    }
}
