package io.github.lucasssoh.boussole.format.model.block;

import io.github.lucasssoh.boussole.format.diagnostic.SourcePosition;

/** A piece of a session's content. Each kind is a directive of {@code FORMAT.md}, except {@link ProseBlock}. */
public sealed interface Block permits ProseBlock, ExampleBlock, RecallBlock, CalloutBlock, VideoBlock, PdfBlock, ExerciseBlock, QcmBlock, ExerciseRefBlock {

    SourcePosition position();
}
