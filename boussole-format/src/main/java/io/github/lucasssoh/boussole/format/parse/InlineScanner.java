package io.github.lucasssoh.boussole.format.parse;

import io.github.lucasssoh.boussole.format.diagnostic.Code;
import io.github.lucasssoh.boussole.format.model.SourceRef;
import io.github.lucasssoh.boussole.format.model.block.InlineDirective;
import io.github.lucasssoh.boussole.format.model.block.PdfMention;
import io.github.lucasssoh.boussole.format.model.block.Prose;
import io.github.lucasssoh.boussole.format.model.block.TermMention;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Finds the inline directives {@code :term[…]} and {@code :pdf[…]} of a text, outside code. */
final class InlineScanner {

    private static final Pattern INLINE = Pattern.compile("(?<![\\w:]):(term|pdf)\\[([^\\]]*)](?:\\{([^}]*)})?");
    private static final Pattern CODE_SPAN = Pattern.compile("(`+)[^`]*?\\1");

    private final ParseContext context;

    InlineScanner(ParseContext context) {
        this.context = context;
    }

    Prose prose(RawNode.Text text) {
        List<InlineDirective> inlines = new ArrayList<>();
        CodeFence fence = new CodeFence();
        for (int index = 0; index < text.lines().size(); index++) {
            String line = text.lines().get(index);
            if (!fence.isCode(line)) {
                scan(withoutCodeSpans(line), text.line() + index, inlines);
            }
        }
        return new Prose(text.markdown(), List.copyOf(inlines), context.position(text.line()));
    }

    private void scan(String line, int number, List<InlineDirective> inlines) {
        Matcher matcher = INLINE.matcher(line);
        while (matcher.find()) {
            String label = matcher.group(2);
            String attributes = Optional.ofNullable(matcher.group(3)).orElse("");
            if (matcher.group(1).equals("term")) {
                context.attributes("term", attributes, Set.of(), Set.of("id"), number)
                        .map(found -> new TermMention(label, found.text("id"), context.position(number)))
                        .ifPresent(inlines::add);
            } else {
                context.attributes("pdf", attributes, Set.of("src"), Set.of("page"), number)
                        .flatMap(found -> pdf(label, found, number))
                        .ifPresent(inlines::add);
            }
        }
    }

    private Optional<InlineDirective> pdf(String label, Attributes attributes, int number) {
        try {
            SourceRef source = new SourceRef(attributes.required("src"), attributes.positiveInteger("page"), context.position(number));
            return Optional.of(new PdfMention(label, source));
        } catch (InvalidAttributeException e) {
            context.report(Code.F027, number, "`pdf`: " + e.getMessage());
            return Optional.empty();
        }
    }

    private static String withoutCodeSpans(String line) {
        return CODE_SPAN.matcher(line).replaceAll(match -> " ".repeat(match.group().length()));
    }
}
