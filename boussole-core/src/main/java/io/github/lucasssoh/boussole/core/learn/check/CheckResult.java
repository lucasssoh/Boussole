package io.github.lucasssoh.boussole.core.learn.check;

import java.util.List;
import java.util.OptionalDouble;

/**
 * The verdict on a response, and how each of its parts was judged. {@code share}, from 0 to 1, is
 * how much of a question that earns no points the response got right, when the verdict alone does
 * not say it: the weight of the criteria met in a rubric.
 */
public record CheckResult(Verdict verdict, List<CheckedItem> items, OptionalDouble share) {

    public CheckResult {
        items = List.copyOf(items);
    }

    public CheckResult(Verdict verdict, List<CheckedItem> items) {
        this(verdict, items, OptionalDouble.empty());
    }

    static CheckResult whole(boolean correct) {
        return new CheckResult(correct ? Verdict.CORRECT : Verdict.WRONG, List.of());
    }

    /** Correct when every item is, partly when some are. */
    static CheckResult ofItems(List<CheckedItem> items) {
        return new CheckResult(Verdict.of(items.stream().filter(CheckedItem::correct).count(), items.size()), items);
    }

    public boolean isCorrect() {
        return verdict == Verdict.CORRECT;
    }
}
