package io.github.lucasssoh.boussole.format.model.expectation;

import java.util.List;

/** An option to pick in a list, such as a drop-down in a fill gap. */
public record ChoiceExpectation(List<String> options, int correct) implements Expectation {

    @Override
    public boolean isChosen() {
        return true;
    }

    public String correctOption() {
        return options.get(correct);
    }
}
