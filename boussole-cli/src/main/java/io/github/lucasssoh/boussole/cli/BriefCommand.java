package io.github.lucasssoh.boussole.cli;

import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCollector;
import io.github.lucasssoh.boussole.format.load.CourseLoader;
import io.github.lucasssoh.boussole.format.model.Course;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

/**
 * {@code boussole brief FOLDER}: where a track stands, for an agent that continues it (new
 * documents, a chapter more) without reading every session again. See {@link TrackBrief}.
 */
@Command(
        name = "brief",
        mixinStandardHelpOptions = true,
        description = {
            "Say where a subject stands, in a few lines, for an agent that continues it: the documents",
            "to take in, each chapter with its documents, sessions and notions, the IDs taken, the lexicon."
        })
final class BriefCommand implements Callable<Integer> {

    @Parameters(paramLabel = "FOLDER", description = "The subject folder, the one holding subject.yaml.")
    private Path folder;

    @Spec
    private CommandSpec spec;

    @Override
    public Integer call() {
        Optional<Course> course = Files.isDirectory(folder) ? CourseLoader.load(folder, new DiagnosticCollector()) : Optional.empty();
        if (course.isEmpty()) {
            spec.commandLine().getErr().println("boussole: " + folder + " has no readable subject.yaml");
            return ExitCode.UNUSABLE;
        }
        spec.commandLine().getOut().print(TrackBrief.of(course.get(), TrackBrief.documents(folder, course.get())));
        spec.commandLine().getOut().flush();
        return ExitCode.OK;
    }
}
