package io.github.lucasssoh.boussole.cli;

import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCollector;
import io.github.lucasssoh.boussole.format.load.CourseLoader;
import io.github.lucasssoh.boussole.format.model.Course;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

/**
 * {@code boussole lock FOLDER [--off]}: says that every document of a track is in. Documents that
 * arrive after are left out, and the final mock exam can be written. See {@link TrackLock}.
 */
@Command(
        name = "lock",
        mixinStandardHelpOptions = true,
        description = {
            "Lock a subject once every document is in: documents added later are left out,",
            "and the final mock exam can be written. --off unlocks it."
        })
final class LockCommand implements Callable<Integer> {

    @Parameters(paramLabel = "FOLDER", description = "The subject folder, the one holding subject.yaml.")
    private Path folder;

    @Option(names = "--off", description = "Unlock the subject: documents added later are taken in again.")
    private boolean off;

    @Spec
    private CommandSpec spec;

    @Override
    public Integer call() {
        PrintWriter out = spec.commandLine().getOut();
        Optional<Course> course = Files.isDirectory(folder) ? CourseLoader.load(folder, new DiagnosticCollector()) : Optional.empty();
        if (course.isEmpty() || course.get().syllabus().chapters().isEmpty()) {
            spec.commandLine().getErr().println("boussole: " + folder + " has no plan to lock");
            return ExitCode.UNUSABLE;
        }
        if (off) {
            TrackLock.unlock(folder);
            out.println(course.get().subject().name() + " unlocked: new documents will be taken in.");
            if (course.get().syllabus().finalExam().isPresent() && TrackBrief.finalExamWritten(course.get())) {
                out.println("Taking a new document in means building the final mock exam again.");
            }
        } else {
            TrackLock.lock(folder, LocalDate.now());
            out.println(course.get().subject().name() + " locked: documents added from now on are left out.");
            course.get().syllabus().finalExam().filter(exam -> !TrackBrief.finalExamWritten(course.get()))
                    .ifPresent(exam -> out.println("The final mock exam (" + exam.id() + ") can now be written."));
        }
        return ExitCode.OK;
    }
}
