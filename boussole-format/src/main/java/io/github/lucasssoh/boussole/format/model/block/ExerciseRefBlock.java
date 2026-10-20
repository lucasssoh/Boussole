package io.github.lucasssoh.boussole.format.model.block;

import io.github.lucasssoh.boussole.format.diagnostic.SourcePosition;

/** {@code ::ref{id=…}}: an exercise defined elsewhere in the subject, asked again. */
public record ExerciseRefBlock(String id, SourcePosition position) implements Block {
}
