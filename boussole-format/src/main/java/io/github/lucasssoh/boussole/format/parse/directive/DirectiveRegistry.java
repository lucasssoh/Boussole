package io.github.lucasssoh.boussole.format.parse.directive;

import io.github.lucasssoh.boussole.format.model.block.CalloutKind;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/** The directives Boussole knows, by name. A new kind of block is a new parser added here. */
public final class DirectiveRegistry {

    private final Map<String, DirectiveParser> parsers;

    public DirectiveRegistry(List<DirectiveParser> parsers) {
        this.parsers = parsers.stream().collect(Collectors.toUnmodifiableMap(parser -> parser.spec().name(), Function.identity()));
    }

    public static DirectiveRegistry standard() {
        return new DirectiveRegistry(List.of(
                new ExampleParser(),
                new RecallParser(),
                new CalloutParser(CalloutKind.NOTE),
                new CalloutParser(CalloutKind.TIP),
                new CalloutParser(CalloutKind.WARNING),
                new VideoParser(),
                new PdfParser(),
                new ExerciseParser(),
                new QcmParser(),
                new RefParser()));
    }

    public Optional<DirectiveParser> find(String name) {
        return Optional.ofNullable(parsers.get(name));
    }
}
