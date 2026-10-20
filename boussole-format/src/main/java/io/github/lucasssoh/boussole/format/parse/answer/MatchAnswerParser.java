package io.github.lucasssoh.boussole.format.parse.answer;

import io.github.lucasssoh.boussole.format.diagnostic.Code;
import io.github.lucasssoh.boussole.format.model.answer.Answer;
import io.github.lucasssoh.boussole.format.model.answer.MatchAnswer;
import io.github.lucasssoh.boussole.format.model.answer.MatchPair;
import io.github.lucasssoh.boussole.format.model.block.ExerciseType;
import io.github.lucasssoh.boussole.format.parse.Attributes;
import io.github.lucasssoh.boussole.format.parse.ParseContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** One pair per line: {@code left => right}. */
final class MatchAnswerParser implements AnswerParser {

    private static final String ARROW = "=>";

    @Override
    public ExerciseType type() {
        return ExerciseType.MATCH;
    }

    @Override
    public Set<String> attributes() {
        return Set.of();
    }

    @Override
    public Optional<Answer> parse(List<AnswerLine> lines, Attributes attributes, ParseContext context) {
        List<MatchPair> pairs = new ArrayList<>();
        for (AnswerLine line : lines) {
            String[] sides = line.text().split(ARROW, -1);
            if (sides.length != 2 || sides[0].isBlank() || sides[1].isBlank()) {
                context.report(Code.F021, line.line(), "a match answer line reads `left => right`");
                return Optional.empty();
            }
            pairs.add(new MatchPair(sides[0].strip(), sides[1].strip()));
        }
        if (pairs.size() < 2) {
            context.report(Code.F021, lines.getFirst().line(), "a match answer needs at least two pairs");
            return Optional.empty();
        }
        return Optional.of(new MatchAnswer(List.copyOf(pairs)));
    }
}
