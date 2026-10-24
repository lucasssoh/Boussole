package io.github.lucasssoh.boussole.core.learn.reward;

/**
 * What the learner keeps for good: their longest streak, the sessions finished, those finished
 * without a mistake, and their first answers: how many were right, out of how many (hooks aside).
 */
public record Records(int bestStreak, int sessionsFinished, int flawlessSessions, int firstTryRight, int firstTryAnswered) {
}
