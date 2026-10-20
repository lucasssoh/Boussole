package io.github.lucasssoh.boussole.core.learn.reward;

/**
 * What the learner has earned: their experience and level, and their streak, the days in a row
 * on which they answered at least once (today included once they have).
 */
public record StudyStats(int xp, Level level, int streakDays, boolean studiedToday) {
}
