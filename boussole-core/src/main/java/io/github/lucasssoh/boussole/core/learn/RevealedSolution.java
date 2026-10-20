package io.github.lucasssoh.boussole.core.learn;

import io.github.lucasssoh.boussole.format.model.block.Block;
import io.github.lucasssoh.boussole.format.model.block.ExerciseSolution;
import java.util.Optional;

/** The question, whose expected answer can now be shown, and its worked solution. */
public record RevealedSolution(Block question, Optional<ExerciseSolution> solution) {
}
