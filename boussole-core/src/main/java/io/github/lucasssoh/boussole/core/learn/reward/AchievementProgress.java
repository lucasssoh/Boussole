package io.github.lucasssoh.boussole.core.learn.reward;

/** Where the learner stands on an achievement: the value counted and the levels it reaches. */
public record AchievementProgress(Achievement achievement, int value, int level) {

    public static AchievementProgress of(Achievement achievement, int value) {
        return new AchievementProgress(achievement, value, achievement.levelOf(value));
    }
}
