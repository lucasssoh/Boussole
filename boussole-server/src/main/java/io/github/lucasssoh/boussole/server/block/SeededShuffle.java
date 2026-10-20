package io.github.lucasssoh.boussole.server.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Shuffles the items of a question the same way every time, so that reloading the page does not
 * move them, and never leaves them in the order of the answer.
 */
final class SeededShuffle {

    private SeededShuffle() {
    }

    static List<String> of(List<String> items, String seed) {
        List<String> shuffled = new ArrayList<>(items);
        Collections.shuffle(shuffled, new Random(seed.hashCode()));
        if (shuffled.equals(items) && items.size() > 1) {
            Collections.rotate(shuffled, 1);
        }
        return List.copyOf(shuffled);
    }
}
