package io.github.lucasssoh.boussole.core.learn.reward;

import io.github.lucasssoh.boussole.core.learn.ContentRepository;
import io.github.lucasssoh.boussole.core.learn.check.Verdict;
import io.github.lucasssoh.boussole.core.learn.progress.ProgressRepository;
import io.github.lucasssoh.boussole.format.model.block.ExerciseBlock;
import io.github.lucasssoh.boussole.format.model.block.McqBlock;
import io.github.lucasssoh.boussole.format.model.Domain;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Points, ranks and streak, computed from the history of attempts and finished sessions of the
 * subjects that are rewarded. A subject's points go to its field (its {@code domain}); a field
 * counts for the culture rank once it holds {@link Ladder#FIELD_SHARE} of all the points.
 */
public final class ReadStudyStats {

    private final ContentRepository content;
    private final ProgressRepository progress;
    private final RewardedSubjects rewarded;
    private final Clock clock;

    public ReadStudyStats(ContentRepository content, ProgressRepository progress, RewardedSubjects rewarded, Clock clock) {
        this.content = content;
        this.progress = progress;
        this.rewarded = rewarded;
        this.clock = clock;
    }

    public StudyStats read() {
        List<AttemptRecord> attempts = progress.attemptHistory().stream()
                .filter(attempt -> rewarded.rewards(attempt.subject()))
                .toList();
        List<SessionFinishedRecord> finished = progress.sessionsFinished().stream()
                .filter(session -> rewarded.rewards(session.subject()))
                .toList();
        Map<String, Double> bySubject = new HashMap<>();
        attempts.forEach(attempt -> bySubject.merge(attempt.subject(), attempt.points(), Double::sum));
        finished.forEach(session -> bySubject.merge(session.subject(), session.bonus(), Double::sum));
        double points = bySubject.values().stream().mapToDouble(Double::doubleValue).sum();

        Map<String, Domain> domains = content.courses().stream()
                .filter(course -> course.subject().domain().isPresent())
                .collect(Collectors.toMap(course -> course.subject().id(), course -> course.subject().domain().orElseThrow(), (a, b) -> a));
        Map<Domain, Double> byField = new EnumMap<>(Domain.class);
        bySubject.forEach((subject, earned) -> Optional.ofNullable(domains.get(subject))
                .ifPresent(domain -> byField.merge(domain, earned, Double::sum)));
        int fields = (int) byField.values().stream().filter(earned -> points > 0 && earned >= Ladder.FIELD_SHARE * points).count();
        List<FieldStanding> standings = byField.entrySet().stream()
                .map(entry -> new FieldStanding(entry.getKey(), entry.getValue(), Rank.of(Ladder.SPECIALTY, entry.getValue())))
                .sorted(Comparator.comparingDouble(FieldStanding::points).reversed())
                .toList();

        Set<LocalDate> days = new TreeSet<>();
        attempts.forEach(attempt -> days.add(day(attempt.at())));
        LocalDate today = LocalDate.now(clock);
        return new StudyStats(points, Rank.of(Ladder.CULTURE, points, fields), standings, streak(days, today), days.contains(today),
                records(attempts, finished, days));
    }

    private Records records(List<AttemptRecord> attempts, List<SessionFinishedRecord> finished, Set<LocalDate> days) {
        Set<String> hooks = content.courses().stream()
                .flatMap(course -> course.blocks().flatMap(block -> switch (block) {
                    case McqBlock mcq when mcq.hook() -> Stream.of(course.subject().id() + "/" + mcq.id());
                    case ExerciseBlock exercise when exercise.hook() -> Stream.of(course.subject().id() + "/" + exercise.id());
                    default -> Stream.<String>empty();
                }))
                .collect(Collectors.toSet());
        Map<String, Verdict> first = new HashMap<>();
        attempts.forEach(attempt -> first.putIfAbsent(attempt.subject() + "/" + attempt.exercise(), attempt.verdict()));
        hooks.forEach(first::remove);
        int right = (int) first.values().stream().filter(verdict -> verdict == Verdict.CORRECT).count();
        int sessions = (int) finished.stream().map(session -> session.subject() + "/" + session.session()).distinct().count();
        int flawless = (int) finished.stream().filter(session -> session.bonus() > 0).count();
        return new Records(longestRun(days), sessions, flawless, right, first.size());
    }

    private static int longestRun(Set<LocalDate> days) {
        int best = 0;
        for (LocalDate day : days) {
            if (!days.contains(day.minusDays(1))) {
                int run = 0;
                while (days.contains(day.plusDays(run))) {
                    run++;
                }
                best = Math.max(best, run);
            }
        }
        return best;
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
