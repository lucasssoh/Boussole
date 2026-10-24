package io.github.lucasssoh.boussole.server.reward;

import io.github.lucasssoh.boussole.core.learn.reward.ReadStudyStats;
import io.github.lucasssoh.boussole.core.learn.reward.Rank;
import io.github.lucasssoh.boussole.core.learn.reward.StudyStats;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class StatsController {

    private final ReadStudyStats readStudyStats;

    StatsController(ReadStudyStats readStudyStats) {
        this.readStudyStats = readStudyStats;
    }

    @GetMapping("/api/stats")
    StatsView stats() {
        return StatsView.of(readStudyStats.read());
    }

    /** Points, the culture rank, the specialty of each field, and the streak of days. */
    record StatsView(double points, RankView culture, List<FieldView> fields, int streakDays, boolean studiedToday) {

        static StatsView of(StudyStats stats) {
            return new StatsView(stats.points(), RankView.of(stats.culture()),
                    stats.fields().stream().map(field -> new FieldView(field.domain().id(), field.points(), RankView.of(field.rank()))).toList(),
                    stats.streakDays(), stats.studiedToday());
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
