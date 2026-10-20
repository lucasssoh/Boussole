package io.github.lucasssoh.boussole.core.learn.check;

import java.util.List;

/** The items of an order exercise, as the learner arranged them. */
public record OrderResponse(List<String> items) implements LearnerResponse {

    public OrderResponse {
        items = List.copyOf(items);
    }
}
