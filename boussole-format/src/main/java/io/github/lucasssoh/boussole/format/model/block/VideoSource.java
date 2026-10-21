package io.github.lucasssoh.boussole.format.model.block;

/** Where a video is played from. */
public sealed interface VideoSource permits YoutubeVideoSource, FileVideoSource {
}
