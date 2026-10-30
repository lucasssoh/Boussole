package io.github.lucasssoh.boussole.cli;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import io.github.lucasssoh.boussole.core.device.DeviceRepository;
import io.github.lucasssoh.boussole.core.device.PairingDesk;
import io.github.lucasssoh.boussole.core.learn.ContentRepository;
import io.github.lucasssoh.boussole.core.learn.progress.ProgressRepository;
import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCode;
import io.github.lucasssoh.boussole.format.model.block.Block;
import io.github.lucasssoh.boussole.format.model.block.ExerciseType;
import io.github.lucasssoh.boussole.format.parse.directive.DirectiveParserRegistry;
import io.github.lucasssoh.boussole.server.block.BlockView;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import picocli.CommandLine.Command;

/**
 * Keeps the documentation in step with the code: each test names what was added in the code and
 * where it must also appear, so that a change cannot land without its docs and diagrams. The
 * recipes of {@code AGENTS.md} say how to add each kind of thing.
 */
class DocumentationSyncTest {

    private static final Path ROOT = Path.of("..");
    private static final Path DIAGRAMS = ROOT.resolve("docs/diagrams");
    private static final Pattern DIAGRAM_TYPE =
            Pattern.compile("^\\s*(?:abstract\\s+)?(?:class|interface|enum)\\s+(\"([^\"]+)\"|([\\w.]+))", Pattern.MULTILINE);

    @Test
    void every_kind_of_block_is_in_the_blocks_diagram() {
        String diagram = read(DIAGRAMS.resolve("11-model-blocks.puml"));

        assertThat(Arrays.stream(Block.class.getPermittedSubclasses()).map(Class::getSimpleName).filter(name -> !declares(diagram, name)))
                .as("Block types missing from docs/diagrams/11-model-blocks.puml")
                .isEmpty();
    }

    @Test
    void every_directive_is_documented_in_the_format_and_drawn_with_its_parser() {
        DirectiveParserRegistry registry = DirectiveParserRegistry.standard();
        String format = read(ROOT.resolve("docs/FORMAT.md"));
        String diagram = read(DIAGRAMS.resolve("20-parsing-structure.puml"));

        assertThat(registry.names().stream().filter(name -> !inTable(format, name)))
                .as("directives missing from docs/FORMAT.md §5.5 or §5.6").isEmpty();
        assertThat(registry.names().stream().map(name -> registry.find(name).orElseThrow().getClass().getSimpleName()).distinct()
                        .filter(parser -> !declares(diagram, parser)))
                .as("directive parsers missing from docs/diagrams/20-parsing-structure.puml").isEmpty();
    }

    @Test
    void every_exercise_type_is_in_the_format() {
        String format = read(ROOT.resolve("docs/FORMAT.md"));

        assertThat(Arrays.stream(ExerciseType.values()).map(ExerciseType::attributeValue).filter(type -> !inTable(format, type)))
                .as("exercise types missing from the table of docs/FORMAT.md §5.6").isEmpty();
    }

    @Test
    void every_diagnostic_code_is_in_the_table_of_the_format() {
        String format = read(ROOT.resolve("docs/FORMAT.md"));

        assertThat(Arrays.stream(DiagnosticCode.values()).map(Enum::name).filter(code -> !format.contains("| `" + code + "` |")))
                .as("diagnostic codes missing from the table of docs/FORMAT.md §6").isEmpty();
    }

    @Test
    void every_block_the_server_sends_has_its_type_in_the_interface() {
        String api = read(ROOT.resolve("web/src/app/core/api.ts"));
        JsonSubTypes subTypes = BlockView.class.getAnnotation(JsonSubTypes.class);

        assertThat(Arrays.stream(subTypes.value()).map(JsonSubTypes.Type::name).filter(name -> !api.contains("type: '" + name + "'")))
                .as("block types the server sends but web/src/app/core/api.ts does not know").isEmpty();
    }

    @Test
    void every_use_case_is_in_the_core_diagram() {
        String diagram = read(DIAGRAMS.resolve("50-core-learn.puml"));
        Path learn = ROOT.resolve("boussole-core/src/main/java/io/github/lucasssoh/boussole/core/learn");

        List<String> useCases = Stream.concat(classesIn(learn, "io.github.lucasssoh.boussole.core.learn"),
                        classesIn(learn.resolve("reward"), "io.github.lucasssoh.boussole.core.learn.reward"))
                .filter(DocumentationSyncTest::isUseCase)
                .map(Class::getSimpleName)
                .toList();

        assertThat(useCases).isNotEmpty();
        assertThat(useCases.stream().filter(name -> !declares(diagram, name)))
                .as("use cases missing from docs/diagrams/50-core-learn.puml").isEmpty();
    }

    @Test
    void every_device_use_case_is_in_the_devices_diagram() {
        String diagram = read(DIAGRAMS.resolve("55-core-devices.puml"));
        Path device = ROOT.resolve("boussole-core/src/main/java/io/github/lucasssoh/boussole/core/device");

        List<String> useCases = classesIn(device, "io.github.lucasssoh.boussole.core.device")
                .filter(type -> Modifier.isPublic(type.getModifiers()) && Arrays.stream(type.getConstructors())
                        .flatMap(constructor -> Arrays.stream(constructor.getParameterTypes()))
                        .anyMatch(parameter -> parameter == PairingDesk.class || parameter == DeviceRepository.class))
                .filter(type -> type != PairingDesk.class)
                .map(Class::getSimpleName)
                .toList();

        assertThat(useCases).isNotEmpty();
        assertThat(useCases.stream().filter(name -> !declares(diagram, name)))
                .as("use cases missing from docs/diagrams/55-core-devices.puml").isEmpty();
    }

    @Test
    void every_command_is_in_the_cli_diagram() {
        String diagram = read(DIAGRAMS.resolve("40-cli.puml"));
        Path cli = ROOT.resolve("boussole-cli/src/main/java/io/github/lucasssoh/boussole/cli");

        List<String> commands = Stream.of(
                        classesIn(cli, "io.github.lucasssoh.boussole.cli"),
                        classesIn(cli.resolve("init"), "io.github.lucasssoh.boussole.cli.init"),
                        classesIn(cli.resolve("serve"), "io.github.lucasssoh.boussole.cli.serve"),
                        classesIn(cli.resolve("devices"), "io.github.lucasssoh.boussole.cli.devices"))
                .flatMap(stream -> stream)
                .filter(type -> type.isAnnotationPresent(Command.class))
                .map(Class::getSimpleName)
                .toList();

        assertThat(commands).isNotEmpty();
        assertThat(commands.stream().filter(name -> !declares(diagram, name)))
                .as("commands missing from docs/diagrams/40-cli.puml").isEmpty();
    }

    @Test
    void every_component_of_the_interface_is_in_its_diagram() {
        String diagram = read(DIAGRAMS.resolve("80-web.puml"));
        Pattern exported = Pattern.compile("export class (\\w+Component)\\b");

        List<String> components = new ArrayList<>();
        walk(ROOT.resolve("web/src/app"), ".component.ts").forEach(file -> {
            Matcher matcher = exported.matcher(read(file));
            while (matcher.find()) {
                components.add(matcher.group(1));
            }
        });

        assertThat(components).isNotEmpty();
        assertThat(components.stream().filter(name -> !declares(diagram, name)))
                .as("Angular components missing from docs/diagrams/80-web.puml").isEmpty();
    }

    @Test
    void every_diagram_is_rendered_since_its_last_change() {
        for (Path source : walk(DIAGRAMS, ".puml")) {
            Path rendering = source.resolveSibling(source.getFileName().toString().replace(".puml", ".svg"));
            assertThat(rendering).as("render %s with docs/diagrams/render.sh", source.getFileName()).exists();
            String svg = read(rendering);
            Matcher matcher = DIAGRAM_TYPE.matcher(read(source));
            while (matcher.find()) {
                // A quoted name is shown as written, on several lines when it holds \n.
                List<String> shown = matcher.group(2) != null
                        ? List.of(matcher.group(2).split("\\\\n"))
                        : List.of(matcher.group(3).substring(matcher.group(3).lastIndexOf('.') + 1));
                for (String text : shown) {
                    // Generic parameters are drawn apart: the name alone tells the rendering is current.
                    String name = text.strip().split("<")[0].strip();
                    assertThat(svg).as("%s shows %s, but %s does not: run docs/diagrams/render.sh",
                            source.getFileName(), name, rendering.getFileName()).contains(escape(name));
                }
            }
        }
    }

    /** Whether a table of the format lists that name, alone or among others: {@code | `a`} or {@code , `a`}. */
    private static boolean inTable(String format, String name) {
        return format.contains("| `" + name + "`") || format.contains(", `" + name + "`");
    }

    /** Whether a diagram declares a type of that name, as {@code class Name} or {@code … as Name}. */
    private static boolean declares(String diagram, String name) {
        return Pattern.compile("(?:class|interface|enum)\\s+(?:\"[^\"]*\"\\s+as\\s+)?" + Pattern.quote(name) + "\\b").matcher(diagram).find();
    }

    /** A use case: a public class whose constructor takes a port of the core. */
    private static boolean isUseCase(Class<?> type) {
        return Modifier.isPublic(type.getModifiers()) && !type.isInterface() && !type.isRecord() && !type.isEnum()
                && Arrays.stream(type.getConstructors()).flatMap(constructor -> Arrays.stream(constructor.getParameterTypes()))
                        .anyMatch(parameter -> parameter == ContentRepository.class || parameter == ProgressRepository.class);
    }

    private static Stream<Class<?>> classesIn(Path folder, String packageName) {
        try (Stream<Path> files = Files.list(folder)) {
            return files.filter(file -> file.toString().endsWith(".java"))
                    .map(file -> file.getFileName().toString().replace(".java", ""))
                    .<Class<?>>map(name -> load(packageName + "." + name))
                    .toList()
                    .stream();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Class<?> load(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(name + " is not on the test classpath", e);
        }
    }

    private static List<Path> walk(Path folder, String suffix) {
        try (Stream<Path> files = Files.walk(folder)) {
            return files.filter(file -> file.toString().endsWith(suffix)).sorted().toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String read(Path file) {
        try {
            return Files.readString(file);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String escape(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
