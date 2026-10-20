package io.github.lucasssoh.boussole.core.learn.check;

import io.github.lucasssoh.boussole.format.model.answer.OrderAnswer;
import java.util.List;
import java.util.stream.IntStream;

/** Each position is checked: an item in its place is right. */
final class OrderAnswerResponseChecker implements ResponseChecker<OrderAnswer, OrderResponse> {

    @Override
    public Class<OrderResponse> responseType() {
        return OrderResponse.class;
    }

    @Override
    public CheckResult check(OrderAnswer answer, OrderResponse response) {
        List<String> expected = answer.items();
        List<String> given = response.items();
        if (!sorted(given).equals(sorted(expected))) {
            throw new ResponseMismatchException("the items given are not the items of the exercise");
        }
        return CheckResult.ofItems(IntStream.range(0, expected.size())
                .mapToObj(index -> CheckedItem.of(index, expected.get(index).equals(given.get(index))))
                .toList());
    }

    private static List<String> sorted(List<String> items) {
        return items.stream().sorted().toList();
    }
}
