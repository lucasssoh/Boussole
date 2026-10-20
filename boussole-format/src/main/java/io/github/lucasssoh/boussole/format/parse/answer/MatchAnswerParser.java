package io.github.lucasssoh.boussole.format.parse.answer;

import io.github.lucasssoh.boussole.format.diagnostic.Code;
import io.github.lucasssoh.boussole.format.model.answer.Answer;
import io.github.lucasssoh.boussole.format.model.answer.MatchAnswer;
import io.github.lucasssoh.boussole.format.model.answer.MatchPair;
import io.github.lucasssoh.boussole.format.model.block.ExerciseType;
import io.github.lucasssoh.boussole.format.parse.Attributes;
import io.github.lucasssoh.boussole.format.parse.ParseContext;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** One pair per line: {@code left => right}. */
final class MatchAnswerParser extends LineAnswerParser<MatchPair> {

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
    protected int minimumItems() {
        return 2;
    }

    @Override
    protected Optional<MatchPair> readLine(AnswerLine line, Attributes attributes, ParseContext context) {
        String[] sides = line.text().split(ARROW, -1);
        if (sides.length != 2 || sides[0].isBlank() || sides[1].isBlank()) {
            context.report(Code.F021, line.line(), "a match answer line reads `left => right`");
            return Optional.empty();
        }
        return Optional.of(new MatchPair(sides[0].strip(), sides[1].strip()));
    }

    @Override
    protected Optional<Answer> assemble(List<MatchPair> pairs, Attributes attributes, AnswerLine first, ParseContext context) {
        return Optional.of(new MatchAnswer(pairs));
    }
}
