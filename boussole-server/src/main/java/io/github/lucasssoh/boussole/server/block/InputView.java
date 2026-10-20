package io.github.lucasssoh.boussole.server.block;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.List;

/** What the learner fills in to answer an exercise, without what is expected. */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "kind")
@JsonSubTypes({
    @JsonSubTypes.Type(value = InputView.Number.class, name = "number"),
    @JsonSubTypes.Type(value = InputView.Text.class, name = "text"),
    @JsonSubTypes.Type(value = InputView.Fill.class, name = "fill"),
    @JsonSubTypes.Type(value = InputView.Order.class, name = "order"),
    @JsonSubTypes.Type(value = InputView.Match.class, name = "match"),
    @JsonSubTypes.Type(value = InputView.Free.class, name = "free"),
    @JsonSubTypes.Type(value = InputView.Unreadable.class, name = "unreadable")
})
public sealed interface InputView {

    record Number(String unit) implements InputView {
    }

    record Text() implements InputView {
    }

    /** The statement holds {@code [[N]]} where each gap goes. */
    record Fill(List<Gap> gaps) implements InputView {
    }

    /** {@code kind} is {@code text}, {@code number} or {@code choice}; {@code options} only for a choice. */
    record Gap(int number, String kind, List<String> options, String unit) {
    }

    /** The items, shuffled. */
    record Order(List<String> items) implements InputView {
    }

    /** The left-hand sides in order, the right-hand sides shuffled. */
    record Match(List<String> lefts, List<String> rights) implements InputView {
    }

    record Free() implements InputView {
    }

    /** The answer of the exercise could not be read (the validator reports why). */
    record Unreadable() implements InputView {
    }
}
