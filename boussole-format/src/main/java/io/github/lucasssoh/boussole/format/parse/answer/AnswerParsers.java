package io.github.lucasssoh.boussole.format.parse.answer;

import io.github.lucasssoh.boussole.format.model.block.ExerciseType;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** One answer parser per exercise type; free exercises have none. */
public final class AnswerParsers {

    private final Map<ExerciseType, AnswerParser> parsers = new EnumMap<>(ExerciseType.class);

    public AnswerParsers(List<AnswerParser> parsers) {
        parsers.forEach(parser -> this.parsers.put(parser.type(), parser));
    }

    public static AnswerParsers standard() {
        return new AnswerParsers(List.of(
                new NumericAnswerParser(),
                new TextAnswerParser(),
                new FillAnswerParser(),
                new OrderAnswerParser(),
                new MatchAnswerParser()));
    }

    public Optional<AnswerParser> forType(ExerciseType type) {
        return Optional.ofNullable(parsers.get(type));
    }
}
