package io.github.lucasssoh.boussole.core.learn.reward;

/**
 * Where some points stand on a {@link Ladder}: the tier and its {@code division} (5, the lowest,
 * to 1; 0 in the top tier), the level from 1, the points into it and its size, and where the next
 * division starts. {@code fieldsMissing}: the fields still missing to rise above, when the points
 * alone would.
 */
public record Rank(Ladder ladder, int tier, int division, int level, double intoLevel, double levelSize, double nextAt, int fieldsMissing) {

    public String tierId() {
        return ladder.tiers().get(tier);
    }

    /** Where the division the learner is in started. */
    public double divisionFrom() {
        return nextAt - ladder.divisionSize(tier);
    }

    public static Rank of(Ladder ladder, double points) {
        return of(ladder, points, Integer.MAX_VALUE);
    }

    /** {@code fields}: how many fields hold at least {@link Ladder#FIELD_SHARE} of the points. */
    public static Rank of(Ladder ladder, double points, int fields) {
        double left = Math.max(0, points);
        int tier = 0;
        while (tier < ladder.top() && left >= ladder.from(tier + 1)) {
            tier++;
        }
        int missing = 0;
        while (tier > 0 && ladder.fieldsFor(tier) > fields) {
            missing = ladder.fieldsFor(tier) - fields;
            tier--;
        }
        double capped = missing > 0 ? Math.nextDown(ladder.from(tier + 1)) : left;
        double divisionSize = ladder.divisionSize(tier);
        double levelSize = divisionSize / Ladder.LEVELS_PER_DIVISION;
        double into = capped - ladder.from(tier);
        int divisionIndex = tier == ladder.top() ? (int) (into / divisionSize) : Math.min(Ladder.DIVISIONS - 1, (int) (into / divisionSize));
        int levelInDivision = Math.min(Ladder.LEVELS_PER_DIVISION - 1, (int) ((into - divisionIndex * divisionSize) / levelSize));
        int levelsBefore = tier * Ladder.DIVISIONS * Ladder.LEVELS_PER_DIVISION;
        int level = levelsBefore + divisionIndex * Ladder.LEVELS_PER_DIVISION + levelInDivision + 1;
        double intoLevel = into - divisionIndex * divisionSize - levelInDivision * levelSize;
        int division = tier == ladder.top() ? 0 : Ladder.DIVISIONS - divisionIndex;
        double nextAt = ladder.from(tier) + (divisionIndex + 1) * divisionSize;
        return new Rank(ladder, tier, division, level, intoLevel, levelSize, nextAt, missing);
    }
}
