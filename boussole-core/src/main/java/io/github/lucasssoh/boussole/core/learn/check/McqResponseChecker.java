package io.github.lucasssoh.boussole.core.learn.check;

import io.github.lucasssoh.boussole.format.model.block.McqBlock;
import io.github.lucasssoh.boussole.format.model.block.McqChoice;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/** Right when the choices ticked are exactly the right ones; each choice ticked brings its feedback. */
final class McqResponseChecker implements ResponseChecker<McqBlock, ChoicesResponse> {

    @Override
    public Class<ChoicesResponse> responseType() {
        return ChoicesResponse.class;
    }

    @Override
    public CheckResult check(McqBlock mcq, ChoicesResponse response) {
        List<McqChoice> choices = mcq.choices();
        if (response.picked().stream().anyMatch(index -> index < 0 || index >= choices.size())) {
            throw new ResponseMismatchException("no such choice in " + mcq.id());
        }
        Set<Integer> right = IntStream.range(0, choices.size())
                .filter(index -> choices.get(index).correct())
                .boxed()
                .collect(Collectors.toSet());
        List<CheckedItem> items = response.picked().stream()
                .sorted()
                .map(index -> new CheckedItem(String.valueOf(index), choices.get(index).correct(), choices.get(index).feedback()))
                .toList();
        long rightPicked = response.picked().stream().filter(right::contains).count();
        Verdict verdict = response.picked().equals(right) ? Verdict.CORRECT : rightPicked > 0 ? Verdict.PARTLY : Verdict.WRONG;
        return new CheckResult(verdict, items);
    }
}
