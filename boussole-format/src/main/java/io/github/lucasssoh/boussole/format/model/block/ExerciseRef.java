package io.github.lucasssoh.boussole.format.model.block;

import io.github.lucasssoh.boussole.format.diagnostic.Position;

/** {@code ::ref{id=…}}: an exercise defined elsewhere in the subject, asked again. */
public record ExerciseRef(String id, Position position) implements Block {
}
