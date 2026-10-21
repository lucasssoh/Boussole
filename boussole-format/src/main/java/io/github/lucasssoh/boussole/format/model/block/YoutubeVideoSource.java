package io.github.lucasssoh.boussole.format.model.block;

/** A YouTube video, by its 11-character ID. */
public record YoutubeVideoSource(String youtubeId) implements VideoSource {
}
