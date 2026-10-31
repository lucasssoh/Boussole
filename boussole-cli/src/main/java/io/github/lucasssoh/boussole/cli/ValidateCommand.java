package io.github.lucasssoh.boussole.cli;

import io.github.lucasssoh.boussole.adapters.config.BoussoleConfigFile;
import io.github.lucasssoh.boussole.format.InspectionReport;
import io.github.lucasssoh.boussole.adapters.video.YoutubeOEmbedCatalog;
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
        InspectionReport report = TrackCheck.of(folder, videos, BoussoleConfigFile.forCurrentUser().read().subjects());
        new ReportPrinter(spec.commandLine().getOut(), folder, errorsOnly).print(report);
        return report.hasErrors() ? ExitCode.PROBLEMS : ExitCode.OK;
    }
}
