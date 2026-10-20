package io.github.lucasssoh.boussole.format.validate;

/** Knows whether a YouTube video exists. Implemented outside this module, since it needs the network. */
public interface VideoCatalog {

    boolean exists(String youtubeId);
}
