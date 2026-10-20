package io.github.lucasssoh.boussole.cli;

import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCollector;
import io.github.lucasssoh.boussole.format.load.CourseLoader;
import io.github.lucasssoh.boussole.format.model.Course;
import io.github.lucasssoh.boussole.format.source.FolderSourceDocuments;
import io.github.lucasssoh.boussole.format.source.SourceInventory;
import io.github.lucasssoh.boussole.format.source.SourceInventory.SourceState;
import io.github.lucasssoh.boussole.format.source.SourceInventory.State;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

/**
 * {@code boussole sources FOLDER}: the documents of the sources folder against
 * {@code cursus/sources.yaml}: what is new, changed, waiting or gone, and the entries to add,
 * their SHA-256 computed, ready to paste.
 */
@Command(
        name = "sources",
        mixinStandardHelpOptions = true,
        description = {
            "List the documents of a subject against cursus/sources.yaml: new, changed, pending, gone.",
            "Prints the entries to add, with their SHA-256, ready to paste."
        })
final class SourcesCommand implements Callable<Integer> {

    @Parameters(paramLabel = "FOLDER", description = "The subject folder, the one holding subject.yaml.")
    private Path folder;

    @Option(names = "--all", description = "Also list the documents already used or ignored.")
    private boolean all;

    @Spec
    private CommandSpec spec;

    @Override
    public Integer call() {
        PrintWriter out = spec.commandLine().getOut();
        Optional<Course> course = Files.isDirectory(folder) ? CourseLoader.load(folder, new DiagnosticCollector()) : Optional.empty();
        if (course.isEmpty()) {
            spec.commandLine().getErr().println("boussole: " + folder + " has no readable subject.yaml");
            return ExitCode.UNUSABLE;
        }
        String sourcesDir = course.get().subject().sourcesDir();
        List<SourceState> states = SourceInventory.compare(course.get().sources(),
                new FolderSourceDocuments(folder.resolve(sourcesDir), folder));
        List<SourceState> toDo = states.stream().filter(state -> state.state().needsWork() || state.state() == State.GONE).toList();
        out.println(folder + " (" + sourcesDir + "): " + toDo.size() + " to take into account, "
                + count(states, State.USED) + " used, " + count(states, State.IGNORED) + " ignored.");
        states.stream()
                .filter(state -> all || toDo.contains(state))
                .forEach(state -> out.printf("  %-8s %s%n", state.state().name().toLowerCase(Locale.ROOT), state.file()));
        List<SourceState> toWrite = states.stream()
                .filter(state -> state.state() == State.NEW || state.state() == State.CHANGED)
                .toList();
        if (!toWrite.isEmpty()) {
            out.println();
            out.println("In cursus/sources.yaml, once each document is taken into the course"
                    + (course.get().sources().isEmpty() ? " (the file does not exist yet)" : "") + ":");
            toWrite.forEach(state -> {
                out.println("- file: " + yamlString(state.file()));
                state.sha256().ifPresent(sha -> out.println("  sha256: " + sha));
                out.println("  status: used        # or ignored, or pending");
            });
        }
        return ExitCode.OK;
    }

    private static long count(List<SourceState> states, State state) {
        return states.stream().filter(found -> found.state() == state).count();
    }

    private static String yamlString(String file) {
        return file.matches("[A-Za-z0-9._/-]+") ? file : "\"" + file.replace("\"", "\\\"") + "\"";
    }
}
