package io.github.lucasssoh.boussole.format.parse.directive;

import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCode;
import io.github.lucasssoh.boussole.format.model.block.McqChoice;
import io.github.lucasssoh.boussole.format.parse.CodeFence;
import io.github.lucasssoh.boussole.format.parse.ParseContext;
import io.github.lucasssoh.boussole.format.parse.RawNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Separates the statement of an MCQ from its choices: a task list, each choice optionally
 * followed by an indented blockquote, its feedback. A task list inside fenced code belongs to the
 * statement: it is shown, not answered.
 */
final class McqChoiceReader {

    private static final Pattern CHOICE = Pattern.compile("[-*+] \\[([ xX])]\\s+(.*)");

    private final ParseContext context;
    private final List<RawNode> statement = new ArrayList<>();
    private final List<McqChoice> choices = new ArrayList<>();
    private final CodeFence fence = new CodeFence();
    private OpenChoice current;

    private McqChoiceReader(ParseContext context) {
        this.context = context;
    }

    record Result(List<RawNode> statement, List<McqChoice> choices) {
    }

    static Result read(List<RawNode> nodes, ParseContext context) {
        McqChoiceReader reader = new McqChoiceReader(context);
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
            boolean inStatement = current == null && choices.isEmpty();
            if (fence.isCode(line) && inStatement) {
                before.add(line);
                continue;
            }
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
                context.report(DiagnosticCode.F022, number, "text after the choices: the choices end the statement of an MCQ");
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

        private McqChoice toChoice(ParseContext context) {
            Optional<String> joined = feedback.isEmpty() ? Optional.empty() : Optional.of(String.join("\n", feedback));
            return new McqChoice(text.toString(), correct, joined, context.position(line));
        }
    }
}
