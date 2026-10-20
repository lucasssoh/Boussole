package io.github.lucasssoh.boussole.core.learn.reward;

/**
 * A level, reached with experience: level {@code n} takes {@code 100 × n} XP to pass, so that
 * each level asks a little more than the one before.
 */
public record Level(int number, int xpInLevel, int size) {

    static final int STEP = 100;

    public static Level of(int xp) {
        int number = 1;
        int left = Math.max(0, xp);
        while (left >= STEP * number) {
            left -= STEP * number;
            number++;
        }
        return new Level(number, left, STEP * number);
    }
}
