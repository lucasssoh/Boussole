package io.github.lucasssoh.boussole.core.learn.progress;

/** An exercise or MCQ of a subject: its ID is unique in the subject. */
public record ExerciseKey(String subject, String exercise) {
}
