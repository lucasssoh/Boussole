package io.github.lucasssoh.boussole.core.learn.progress;

/**
 * How far the learner is in a session: of its questions (those it asks again with {@code ::ref}
 * included), how many they have answered, and how many they have got right.
 */
public record SessionProgress(int questions, int answered, int right) {
}
