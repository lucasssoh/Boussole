package io.github.lucasssoh.boussole.core.learn.reward;

import java.util.ArrayList;
import java.util.List;

/**
 * A scale of ranks: tiers, each split into five divisions (V, the lowest, to I) of
 * {@link #LEVELS_PER_DIVISION} levels, then a top tier without divisions and without end.
 * {@code from} is the points each tier starts at, the top one last; a tier's divisions are equal,
 * and each tier asks far more than the one before.
 *
 * <ul>
 *   <li>{@link #CULTURE}, the general rank: breadth. Sage and Grand Sage also ask for several
 *       fields ({@link #fieldsFor(int)}).</li>
 *   <li>{@link #SPECIALTY}, one rank per field: depth.</li>
 * </ul>
 */
public enum Ladder {
    CULTURE(List.of("curious", "apprentice", "initiate", "scholar", "savant", "master", "sage", "grand-sage"),
            new double[] {0, 1227, 4948, 16232, 50448, 118627, 228329, 404845}),
    SPECIALTY(List.of("novice", "practitioner", "proficient", "expert", "specialist", "authority", "luminary"),
            new double[] {0, 1200, 4800, 16000, 32000, 50000, 80000});

    public static final int DIVISIONS = 5;
    public static final int LEVELS_PER_DIVISION = 14;
    /** The share of the points a field must hold to count among the fields of a culture rank. */
    public static final double FIELD_SHARE = 0.10;

    private final List<String> tiers;
    private final double[] from;

    Ladder(List<String> tiers, double[] from) {
        this.tiers = tiers;
        this.from = from;
    }

    public List<String> tiers() {
        return tiers;
    }

    public int top() {
        return tiers.size() - 1;
    }

    /** Where a tier starts. */
    public double from(int tier) {
        return from[tier];
    }

    /** The points a division of this tier spans; the top tier keeps the size of the last one. */
    double divisionSize(int tier) {
        int last = Math.min(tier, top() - 1);
        return (from[last + 1] - from[last]) / DIVISIONS;
    }

    /** One division of the ladder: its tier, its number (5 to 1; 0 for the top tier) and where it starts. */
    public record Step(int tier, int division, double from) {
    }

    /** Every division, from Curious V (or Novice V) to the start of the top tier. */
    public List<Step> steps() {
        List<Step> steps = new ArrayList<>();
        for (int tier = 0; tier < top(); tier++) {
            for (int index = 0; index < DIVISIONS; index++) {
                steps.add(new Step(tier, DIVISIONS - index, from[tier] + index * divisionSize(tier)));
            }
        }
        steps.add(new Step(top(), 0, from[top()]));
        return List.copyOf(steps);
    }

    /** The fields a culture tier asks for: 3 for Sage, 5 for Grand Sage, else 1. */
    public int fieldsFor(int tier) {
        if (this != CULTURE) {
            return 1;
        }
        return tier >= top() ? 5 : tier == top() - 1 ? 3 : 1;
    }
}
