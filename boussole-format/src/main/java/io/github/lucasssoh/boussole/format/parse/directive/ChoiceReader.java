package io.github.lucasssoh.boussole.format.parse.directive;

import io.github.lucasssoh.boussole.format.diagnostic.Code;
import io.github.lucasssoh.boussole.format.model.block.Choice;
import io.github.lucasssoh.boussole.format.parse.ParseContext;
import io.github.lucasssoh.boussole.format.parse.RawNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Separates the statement of a qcm from its choices: a task list, each choice optionally
 * followed by an indented blockquote, its feedback.
 */
final class ChoiceReader {

    private static final Pattern CHOICE = Pattern.compile("[-*+] \\[([ xX])]\\s+(.*)");

    private final ParseContext context;
    private final List<RawNode> statement = new ArrayList<>();
    private final List<Choice> choices = new ArrayList<>();
    private OpenChoice current;

    private ChoiceReader(ParseContext context) {
        this.context = context;
    }

    record Result(List<RawNode> statement, List<Choice> choices) {
    }

    static Result read(List<RawNode> nodes, ParseContext context) {
        ChoiceReader reader = new ChoiceReader(context);
        nodes.forEach(reader::read);
        reader.finishChoice();
        return new Result(List.copyOf(reader.statement), List.copyOf(reader.choices));
    }

    private void read(RawNode node) {
        if (!(node instanceof RawNode.Text text)) {
            statement.add(node);
            return;
        }
        List<String> before = new ArrayList<>();
        for (int index = 0; index < text.lines().size(); index++) {
            String line = text.lines().get(index);
            int number = text.line() + index;
            Matcher choice = CHOICE.matcher(line);
            if (choice.matches()) {
                finishChoice();
                current = new OpenChoice(choice.group(2).strip(), !choice.group(1).isBlank(), number);
            } else if (current == null && choices.isEmpty()) {
                before.add(line);
            } else if (line.isBlank()) {
                continue;
            } else if (Character.isWhitespace(line.charAt(0)) && current != null) {
                current.add(line.strip());
            } else {
                context.report(Code.F022, number, "text after the choices: the choices end the statement of a qcm");
            }
        }
        if (!before.isEmpty()) {
            statement.add(new RawNode.Text(List.copyOf(before), text.line()));
        }
    }

    private void finishChoice() {
        if (current != null) {
            choices.add(current.toChoice(context));
            current = null;
        }
    }

    private static final class OpenChoice {

        private final StringBuilder text;
        private final boolean correct;
        private final int line;
        private final List<String> feedback = new ArrayList<>();

        private OpenChoice(String text, boolean correct, int line) {
            this.text = new StringBuilder(text);
            this.correct = correct;
            this.line = line;
        }

        private void add(String continuation) {
            if (continuation.startsWith(">")) {
                feedback.add(continuation.substring(1).strip());
            } else {
                text.append(' ').append(continuation);
            }
        }

        private Choice toChoice(ParseContext context) {
            Optional<String> joined = feedback.isEmpty() ? Optional.empty() : Optional.of(String.join("\n", feedback));
            return new Choice(text.toString(), correct, joined, context.position(line));
        }
    }
}
