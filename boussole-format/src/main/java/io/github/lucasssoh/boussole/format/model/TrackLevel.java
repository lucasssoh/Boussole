package io.github.lucasssoh.boussole.format.model;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/**
 * How demanding a track is, by what it takes to follow it: the points of its questions are
 * multiplied by {@link #coefficient()}. Written in {@code subject.yaml} as {@code level}.
 */
public enum TrackLevel {
    DISCOVERY(1.0),
    SECONDARY(1.5),
    BACHELOR(2.0),
    MASTER(3.0),
    RESEARCH(4.0);

    private final double coefficient;

    TrackLevel(double coefficient) {
        this.coefficient = coefficient;
    }

    public double coefficient() {
        return coefficient;
    }

    /** As written in {@code subject.yaml}: {@code discovery}, {@code master}… */
    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static Optional<TrackLevel> of(String id) {
        return Arrays.stream(values()).filter(level -> level.id().equals(id)).findFirst();
    }
}
