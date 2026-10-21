package io.github.lucasssoh.boussole.cli;

import java.util.List;
import java.util.Comparator;
import java.util.ArrayList;
import io.github.lucasssoh.boussole.format.version.TrackVersions;
import io.github.lucasssoh.boussole.format.model.Course;
import io.github.lucasssoh.boussole.format.diagnostic.Diagnostic;
import io.github.lucasssoh.boussole.adapters.config.BoussoleConfigFile;
import io.github.lucasssoh.boussole.format.InspectionReport;
import io.github.lucasssoh.boussole.adapters.video.YoutubeOEmbedCatalog;
import io.github.lucasssoh.boussole.format.SubjectInspector;
import io.github.lucasssoh.boussole.format.validate.Validator;
import io.github.lucasssoh.boussole.format.validate.VideoCatalog;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

/** {@code boussole validate FOLDER}: checks a subject folder against the course format. */
@Command(
        name = "validate",
        mixinStandardHelpOptions = true,
        description = "Check a subject folder against the course format (docs/FORMAT.md).",
        exitCodeListHeading = "%nExit status:%n",
        exitCodeList = {
            "0:no error (warnings are allowed)",
            "1:errors found",
            "2:the folder cannot be read"
        })
final class ValidateCommand implements Callable<Integer> {

    @Parameters(paramLabel = "FOLDER", description = "The subject folder, the one holding subject.yaml.")
    private Path folder;

    @Option(names = "--errors-only", description = "Leave warnings out.")
    private boolean errorsOnly;

    @Option(names = "--check-videos", description = "Also ask YouTube whether each video exists (needs the network).")
    private boolean checkVideos;

    @Spec
    private CommandSpec spec;

    @Override
    public Integer call() {
        if (!Files.isDirectory(folder)) {
            spec.commandLine().getErr().println("boussole: " + folder + " is not a folder");
            return ExitCode.UNUSABLE;
        }
        Optional<VideoCatalog> videos = checkVideos ? Optional.of(new YoutubeOEmbedCatalog()) : Optional.empty();
        InspectionReport report = withOtherVersions(new SubjectInspector(Validator.standard(), videos).inspect(folder));
        new ReportPrinter(spec.commandLine().getOut(), folder, errorsOnly).print(report);
        return report.hasErrors() ? ExitCode.PROBLEMS : ExitCode.OK;
    }

    /**
     * The other versions of the track, in other languages, found next to the folder or among the
     * subjects of config.yaml, compared with this one (F040, F041).
     */
    private InspectionReport withOtherVersions(InspectionReport report) {
        if (report.course().isEmpty()) {
            return report;
        }
        Course course = report.course().get();
        List<Diagnostic> diagnostics = new ArrayList<>(report.diagnostics());
        TrackVersionFolders.of(folder, course.subject().id(), BoussoleConfigFile.forCurrentUser().read().subjects()).stream()
                .map(other -> new SubjectInspector().inspect(other).course())
                .flatMap(Optional::stream)
                .forEach(other -> diagnostics.addAll(TrackVersions.compare(other, course)));
        diagnostics.sort(Comparator.comparing((Diagnostic diagnostic) -> diagnostic.position().file())
                .thenComparingInt(diagnostic -> diagnostic.position().line()));
        return new InspectionReport(report.course(), List.copyOf(diagnostics));
    }
}
