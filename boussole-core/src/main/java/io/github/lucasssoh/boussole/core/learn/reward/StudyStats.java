package io.github.lucasssoh.boussole.core.learn.reward;

import java.util.List;

/**
 * What the learner has earned: their points, their culture rank, their specialty in each field
 * (most points first), and their streak, the days in a row they studied, with whether today counts
 * yet.
 */
public record StudyStats(double points, Rank culture, List<FieldStanding> fields, int streakDays, boolean studiedToday) {

    public StudyStats {
        fields = List.copyOf(fields);
    }
}
