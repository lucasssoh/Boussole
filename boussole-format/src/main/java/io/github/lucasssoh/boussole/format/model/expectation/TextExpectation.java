package io.github.lucasssoh.boussole.format.model.expectation;

import java.util.List;

/** A precise text, one of several accepted spellings; unless {@code strict}, case is ignored. */
public record TextExpectation(List<AcceptedText> accepted, boolean strict) implements Expectation {

    @Override
    public boolean isChosen() {
        return false;
    }
}
