package io.github.lucasssoh.boussole.format.model.block;

import io.github.lucasssoh.boussole.format.diagnostic.Position;

/** A piece of a session's content. Each kind is a directive of {@code FORMAT.md}, except {@link Prose}. */
public sealed interface Block permits Prose, Example, Recall, Callout, Video, PdfRef, Exercise, Qcm, ExerciseRef {

    Position position();
}
