package io.github.lucasssoh.boussole.core.learn.check;

import java.util.List;

/** The verdict on a response, and how each of its parts was judged. */
public record CheckResult(Verdict verdict, List<CheckedItem> items) {

    public CheckResult {
        items = List.copyOf(items);
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
