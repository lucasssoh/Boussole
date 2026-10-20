package io.github.lucasssoh.boussole.core.learn;

import io.github.lucasssoh.boussole.format.model.block.ExerciseHint;

/** A hint just revealed: its number, from 1, out of how many the exercise has. */
public record RevealedHint(int number, int total, ExerciseHint hint) {
}
