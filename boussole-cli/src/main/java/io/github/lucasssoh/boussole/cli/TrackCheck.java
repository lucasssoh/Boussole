package io.github.lucasssoh.boussole.cli;

import io.github.lucasssoh.boussole.format.InspectionReport;
import io.github.lucasssoh.boussole.format.SubjectInspector;
import io.github.lucasssoh.boussole.format.diagnostic.Diagnostic;
import io.github.lucasssoh.boussole.format.model.Course;
import io.github.lucasssoh.boussole.format.validate.Validator;
import io.github.lucasssoh.boussole.format.validate.VideoCatalog;
import io.github.lucasssoh.boussole.format.version.TrackVersions;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** A track checked as {@code boussole validate} and {@code boussole} alone check it. */
public final class TrackCheck {

    private TrackCheck() {
    }

    /**
     * The format, then the other versions of the track, in other languages, found next to the
     * folder or among {@code subjects}, compared with this one (F040, F041).
     */
    public static InspectionReport of(Path folder, Optional<VideoCatalog> videos, List<Path> subjects) {
        InspectionReport report = new SubjectInspector(Validator.standard(), videos).inspect(folder);
        if (report.course().isEmpty()) {
            return report;
        }
        Course course = report.course().get();
        List<Diagnostic> diagnostics = new ArrayList<>(report.diagnostics());
        TrackVersionFolders.of(folder, course.subject().id(), subjects).stream()
                .map(other -> new SubjectInspector().inspect(other).course())
                .flatMap(Optional::stream)
                .forEach(other -> diagnostics.addAll(TrackVersions.compare(other, course)));
        diagnostics.sort(Comparator.comparing((Diagnostic diagnostic) -> diagnostic.position().file())
                .thenComparingInt(diagnostic -> diagnostic.position().line()));
        return new InspectionReport(report.course(), List.copyOf(diagnostics));
    }
}
