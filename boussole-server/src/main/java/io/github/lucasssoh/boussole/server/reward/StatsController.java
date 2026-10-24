package io.github.lucasssoh.boussole.server.reward;

import io.github.lucasssoh.boussole.core.learn.reward.ReadStudyStats;
import io.github.lucasssoh.boussole.core.learn.reward.Ladder;
import io.github.lucasssoh.boussole.core.learn.reward.ReadAchievements;
import io.github.lucasssoh.boussole.core.learn.reward.Rank;
import io.github.lucasssoh.boussole.core.learn.reward.Records;
import io.github.lucasssoh.boussole.core.learn.reward.StudyStats;
import java.util.List;
import java.util.stream.IntStream;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class StatsController {

    private final ReadStudyStats readStudyStats;
    private final ReadAchievements readAchievements;

    StatsController(ReadStudyStats readStudyStats, ReadAchievements readAchievements) {
        this.readStudyStats = readStudyStats;
        this.readAchievements = readAchievements;
    }

    /** Every achievement: the value counted, the levels reached, and the values each level asks. */
    @GetMapping("/api/achievements")
    List<AchievementView> achievements() {
        return readAchievements.read().stream()
                .map(progress -> new AchievementView(progress.achievement().id(), progress.value(), progress.level(),
                        progress.achievement().thresholds()))
                .toList();
    }

    record AchievementView(String id, int value, int level, List<Integer> thresholds) {
    }

    @GetMapping("/api/stats")
    StatsView stats() {
        return StatsView.of(readStudyStats.read());
    }

    /** Both ladders, every division with where it starts and its first level. */
    @GetMapping("/api/ranks")
    LaddersView ranks() {
        return new LaddersView(StepView.all(Ladder.CULTURE), StepView.all(Ladder.SPECIALTY));
    }

    /**
     * Points, the culture rank, the specialty of each field, the streak of days, the records, and
     * the route: the divisions around the learner's, two behind and five ahead.
     */
    record StatsView(double points, RankView culture, List<FieldView> fields, int streakDays, boolean studiedToday,
            Records records, List<StepView> route) {

        static StatsView of(StudyStats stats) {
            return new StatsView(stats.points(), RankView.of(stats.culture()),
                    stats.fields().stream().map(field -> new FieldView(field.domain().id(), field.points(), RankView.of(field.rank()))).toList(),
                    stats.streakDays(), stats.studiedToday(), stats.records(), route(stats.culture()));
        }

        private static List<StepView> route(Rank rank) {
            List<StepView> steps = StepView.all(Ladder.CULTURE);
            int here = 0;
            for (int index = 0; index < steps.size(); index++) {
                StepView step = steps.get(index);
                if (step.tier().equals(rank.tierId()) && step.division() == Math.max(rank.division(), 0)) {
                    here = index;
                }
            }
            return steps.subList(Math.max(0, here - 2), Math.min(steps.size(), here + 6));
        }
    }

    record LaddersView(List<StepView> culture, List<StepView> specialty) {
    }

    /** A division: its tier, its number (5 to 1; 0 for the top tier), where it starts and its first level. */
    record StepView(String tier, int division, double from, int firstLevel) {

        static List<StepView> all(Ladder ladder) {
            List<Ladder.Step> steps = ladder.steps();
            return IntStream.range(0, steps.size())
                    .mapToObj(index -> new StepView(ladder.tiers().get(steps.get(index).tier()), steps.get(index).division(),
                            Math.ceil(steps.get(index).from()), index * Ladder.LEVELS_PER_DIVISION + 1))
                    .toList();
        }
    }

    /**
     * A rank: its tier, its division (5 to 1, 0 in the top tier), its level and how far into it,
     * where its division starts and the next one, and the fields still missing to rise above.
     */
    record RankView(String tier, int division, int level, double intoLevel, double levelSize, double divisionFrom, double nextAt,
            int fieldsMissing) {

        static RankView of(Rank rank) {
            return new RankView(rank.tierId(), rank.division(), rank.level(), rank.intoLevel(), rank.levelSize(), rank.divisionFrom(),
                    rank.nextAt(), rank.fieldsMissing());
        }
    }

    record FieldView(String domain, double points, RankView specialty) {
    }
}
