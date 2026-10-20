package io.github.lucasssoh.boussole.format.parse;

import io.github.lucasssoh.boussole.format.diagnostic.Code;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Turns the lines of a session body into {@link RawNode}s: containers with their nested lines,
 * one-line directives, level-2 headings and text. Lines of fenced code are always text.
 *
 * <p>A closing line closes the innermost open container that has as many colons. Containers
 * still open inside it, or at the end of the file, are reported as not closed.
 */
final class RawTreeBuilder {

    private static final Pattern HEADING = Pattern.compile(" {0,3}(#{1,6})\\s+(.*?)\\s*");
    private static final Pattern HEADING_ID = Pattern.compile("(.*?)\\s*\\{#([^}\\s]*)}");

    private final ParseContext context;
    private final Deque<OpenContainer> open = new ArrayDeque<>();
    private final List<RawNode> top = new ArrayList<>();
    private final CodeFence fence = new CodeFence();
    private List<String> text = new ArrayList<>();
    private int textLine;

    RawTreeBuilder(ParseContext context) {
        this.context = context;
    }

    List<RawNode> build(List<String> lines, int firstLine) {
        for (int index = 0; index < lines.size(); index++) {
            read(lines.get(index), firstLine + index);
        }
        flushText();
        while (!open.isEmpty()) {
            OpenContainer unclosed = open.pop();
            context.report(Code.F026, unclosed.line, "`" + unclosed.directive.name() + "` is never closed");
            attach(unclosed.toNode());
        }
        return List.copyOf(top);
    }

    private void read(String line, int number) {
        if (fence.isCode(line)) {
            addText(line, number);
            return;
        }
        Optional<Integer> closing = DirectiveLine.closing(line);
        if (closing.isPresent()) {
            close(closing.get(), number);
            return;
        }
        Optional<DirectiveLine> container = DirectiveLine.container(line);
        if (container.isPresent()) {
            flushText();
            open.push(new OpenContainer(container.get(), number));
            return;
        }
        Optional<DirectiveLine> leaf = DirectiveLine.leaf(line);
        if (leaf.isPresent()) {
            flushText();
            attach(new RawNode.Leaf(leaf.get(), number));
            return;
        }
        Matcher heading = HEADING.matcher(line);
        if (heading.matches() && heading.group(1).length() <= 2) {
            readHeading(heading, line, number);
            return;
        }
        addText(line, number);
    }

    private void readHeading(Matcher heading, String line, int number) {
        if (heading.group(1).length() == 1) {
            context.report(Code.F012, number, "level-1 heading: the title of a session comes from the syllabus");
            addText(line, number);
            return;
        }
        if (!open.isEmpty()) {
            addText(line, number);
            return;
        }
        flushText();
        Matcher withId = HEADING_ID.matcher(heading.group(2));
        attach(withId.matches()
                ? new RawNode.Heading(withId.group(1), Optional.of(withId.group(2)), number)
                : new RawNode.Heading(heading.group(2), Optional.empty(), number));
    }

    private void close(int colons, int number) {
        if (open.stream().noneMatch(container -> container.directive.colons() == colons)) {
            context.report(Code.F026, number, "closing line with " + colons + " colons, but no container is open with as many");
            return;
        }
        flushText();
        while (true) {
            OpenContainer closed = open.pop();
            if (closed.directive.colons() == colons) {
                attach(closed.toNode());
                return;
            }
            context.report(Code.F026, closed.line, "`" + closed.directive.name() + "` is never closed");
            attach(closed.toNode());
        }
    }

    private void addText(String line, int number) {
        if (text.isEmpty()) {
            textLine = number;
        }
        text.add(line);
    }

    private void flushText() {
        if (!text.isEmpty()) {
            attach(new RawNode.Text(List.copyOf(text), textLine));
            text = new ArrayList<>();
        }
    }

    private void attach(RawNode node) {
        if (open.isEmpty()) {
            top.add(node);
        } else {
            open.peek().children.add(node);
        }
    }

    private static final class OpenContainer {

        private final DirectiveLine directive;
        private final int line;
        private final List<RawNode> children = new ArrayList<>();

        private OpenContainer(DirectiveLine directive, int line) {
            this.directive = directive;
            this.line = line;
        }

        private RawNode toNode() {
            return new RawNode.Container(directive, List.copyOf(children), line);
        }
    }
}
