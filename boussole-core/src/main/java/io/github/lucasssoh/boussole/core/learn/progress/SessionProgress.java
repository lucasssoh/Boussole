package io.github.lucasssoh.boussole.core.learn.progress;

/**
 * How far the learner is in a session: of its questions (those it asks again with {@code ::ref}
 * included), how many they have answered, and how many they have got right; and the stars of the
 * first answers given in it to its own questions that earn points, out of {@code possibleStars}
 * (3 for each such question).
 */
public record SessionProgress(int questions, int answered, int right, int stars, int possibleStars) {

    public static final SessionProgress NONE = new SessionProgress(0, 0, 0, 0, 0);
}
