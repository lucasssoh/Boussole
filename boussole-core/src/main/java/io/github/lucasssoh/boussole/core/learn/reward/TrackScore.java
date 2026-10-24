package io.github.lucasssoh.boussole.core.learn.reward;

/** The points of a track or a session: earned, missed for good, out of all those it gives. */
public record TrackScore(double earned, double missed, double possible) {

    public static final TrackScore NONE = new TrackScore(0, 0, 0);

    /** The points still to play for. */
    public double toPlay() {
        return Math.max(0, possible - earned - missed);
    }
}
