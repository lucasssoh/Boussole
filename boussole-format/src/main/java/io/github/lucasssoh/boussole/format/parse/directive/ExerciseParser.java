package io.github.lucasssoh.boussole.format.parse.directive;

import io.github.lucasssoh.boussole.format.diagnostic.Code;
import io.github.lucasssoh.boussole.format.model.answer.Answer;
import io.github.lucasssoh.boussole.format.model.block.Block;
import io.github.lucasssoh.boussole.format.model.block.Exercise;
import io.github.lucasssoh.boussole.format.model.block.ExerciseType;
import io.github.lucasssoh.boussole.format.parse.InvalidAttributeException;
import io.github.lucasssoh.boussole.format.parse.ParseContext;
import io.github.lucasssoh.boussole.format.parse.RawNode;
import io.github.lucasssoh.boussole.format.parse.answer.AnswerLine;
import io.github.lucasssoh.boussole.format.parse.answer.AnswerParser;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/** {@code ::::exercise{#id type=…}}: the statement, then its answer, hints and solution. */
final class ExerciseParser implements DirectiveParser {

    private static final DirectiveSpec SPEC = new DirectiveSpec(
            "exercise", DirectiveKind.CONTAINER, Set.of("id", "type"), Set.of("tolerance", "unit", "strict", "minutes"), Parents.FLOW);

    /** Attributes every type accepts, besides {@code id} and {@code type}. */
    private static final Set<String> COMMON = Set.of("minutes");

    @Override
    public DirectiveSpec spec() {
        return SPEC;
    }

    @Override
    public Optional<Block> parse(Directive directive, ParseContext context) {
        String id = directive.attributes().required("id");
        ExerciseType type = type(directive);
        Optional<AnswerParser> answerParser = context.answerParsers().forType(type);
        checkTypeAttributes(directive, type, answerParser.map(AnswerParser::attributes).orElse(Set.of()));

        ExerciseParts parts = ExerciseParts.split(directive, context);
        Optional<Answer> answer = answerParser.isEmpty()
                ? noAnswer(parts, context)
                : answer(directive, parts, answerParser.get(), context);
        return Optional.of(new Exercise(
                id,
                type,
                context.blocks(parts.statement(), SPEC.name()),
                answer,
                parts.hints(),
                parts.solution(directive, context),
                directive.attributes().positiveInteger("minutes"),
                context.position(directive.line())));
    }

    private static ExerciseType type(Directive directive) {
        String value = directive.attributes().required("type");
        return ExerciseType.fromAttribute(value).orElseThrow(() -> new InvalidAttributeException("type", "must be one of "
                + Arrays.stream(ExerciseType.values()).map(ExerciseType::attributeValue).collect(Collectors.joining(", "))
                + ", not `" + value + "`"));
    }

    private static void checkTypeAttributes(Directive directive, ExerciseType type, Set<String> allowed) {
        for (String name : directive.attributes().names()) {
            if (!SPEC.required().contains(name) && !COMMON.contains(name) && !allowed.contains(name)) {
                throw new InvalidAttributeException(name, "is not an attribute of a " + type.attributeValue() + " exercise");
            }
        }
    }

    private static Optional<Answer> noAnswer(ExerciseParts parts, ParseContext context) {
        if (!parts.answers().isEmpty()) {
            context.report(Code.F021, parts.answers().getFirst().line(),
                    "a free exercise has no `answer`: the learner compares with the solution");
        }
        return Optional.empty();
    }

    private static Optional<Answer> answer(
            Directive directive, ExerciseParts parts, AnswerParser parser, ParseContext context) {
        if (parts.answers().size() != 1) {
            context.report(Code.F021, directive.line(), "`exercise` needs exactly one `answer`, it has " + parts.answers().size());
            return Optional.empty();
        }
        RawNode.Container answer = parts.answers().getFirst();
        List<AnswerLine> lines = new ArrayList<>();
        for (RawNode child : answer.children()) {
            if (!(child instanceof RawNode.Text text)) {
                context.report(Code.F021, child.line(), "`answer` holds only text, one accepted answer per line");
                return Optional.empty();
            }
            for (int index = 0; index < text.lines().size(); index++) {
                if (!text.lines().get(index).isBlank()) {
                    lines.add(new AnswerLine(text.lines().get(index).strip(), text.line() + index));
                }
            }
        }
        if (lines.isEmpty()) {
            context.report(Code.F021, answer.line(), "`answer` is empty");
            return Optional.empty();
        }
        return parser.parse(lines, directive.attributes(), context);
    }
}
