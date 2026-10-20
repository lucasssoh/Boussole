package io.github.lucasssoh.boussole.core.learn.check;

import java.util.Optional;

/**
 * One checked part of a response: an MCQ choice ticked (its index), a gap (its number), a position
 * of an order exercise (from 0) or a left-hand side of a match exercise. The expected value is
 * never given here: it comes with the solution.
 */
public record CheckedItem(String key, boolean correct, Optional<String> feedback) {

    static CheckedItem of(Object key, boolean correct) {
        return new CheckedItem(String.valueOf(key), correct, Optional.empty());
    }
}
