package io.github.lucasssoh.boussole.format.parse.directive;

import io.github.lucasssoh.boussole.format.model.block.CalloutKind;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/** The directives Boussole knows, by name. A new kind of block is a new parser added here. */
public final class DirectiveParserRegistry {

    private final Map<String, DirectiveParser> parsers;

    public DirectiveParserRegistry(List<DirectiveParser> parsers) {
        this.parsers = parsers.stream().collect(Collectors.toUnmodifiableMap(parser -> parser.spec().name(), Function.identity()));
    }

    public static DirectiveParserRegistry standard() {
        return new DirectiveParserRegistry(List.of(
                new ExampleDirectiveParser(),
                new RecallDirectiveParser(),
                new CalloutDirectiveParser(CalloutKind.NOTE),
                new CalloutDirectiveParser(CalloutKind.TIP),
                new CalloutDirectiveParser(CalloutKind.WARNING),
                new VideoDirectiveParser(),
                new PdfDirectiveParser(),
                new GraphDirectiveParser(),
                new ExerciseDirectiveParser(),
                new McqDirectiveParser(),
                new RefDirectiveParser()));
    }

    /** The names of the directives known, such as {@code example} or {@code graph}. */
    public Set<String> names() {
        return parsers.keySet();
    }

    public Optional<DirectiveParser> find(String name) {
        return Optional.ofNullable(parsers.get(name));
    }
}
