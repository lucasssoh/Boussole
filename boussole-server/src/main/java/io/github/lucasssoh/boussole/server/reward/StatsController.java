package io.github.lucasssoh.boussole.server.reward;

import io.github.lucasssoh.boussole.core.learn.reward.ReadStudyStats;
import io.github.lucasssoh.boussole.core.learn.reward.StudyStats;
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

    /** Experience, level (XP inside it out of its size) and streak of days. */
    record StatsView(int xp, int level, int levelXp, int levelSize, int streakDays, boolean studiedToday) {

        static StatsView of(StudyStats stats) {
            return new StatsView(stats.xp(), stats.level().number(), stats.level().xpInLevel(), stats.level().size(),
                    stats.streakDays(), stats.studiedToday());
        }
    }
}
