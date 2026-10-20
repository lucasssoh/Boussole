package io.github.lucasssoh.boussole.format;

import io.github.lucasssoh.boussole.format.diagnostic.Diagnostic;
import io.github.lucasssoh.boussole.format.diagnostic.Diagnostics;
import io.github.lucasssoh.boussole.format.load.SubjectLoader;
import io.github.lucasssoh.boussole.format.model.Course;
import io.github.lucasssoh.boussole.format.source.FolderSourceDocuments;
import io.github.lucasssoh.boussole.format.validate.ValidationContext;
import io.github.lucasssoh.boussole.format.validate.Validator;
import io.github.lucasssoh.boussole.format.validate.VideoCatalog;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Optional;

/** The entry point of the module: reads a subject folder and checks it against {@code FORMAT.md}. */
public final class SubjectInspector {

    private static final Comparator<Diagnostic> BY_PLACE = Comparator
            .comparing((Diagnostic diagnostic) -> diagnostic.position().file())
            .thenComparingInt(diagnostic -> diagnostic.position().line());

    private final Validator validator;
    private final Optional<VideoCatalog> videos;

    /** An inspector that does not check videos, and so needs no network. */
    public SubjectInspector() {
        this(Validator.standard(), Optional.empty());
    }

    public SubjectInspector(Validator validator, Optional<VideoCatalog> videos) {
        this.validator = validator;
        this.videos = videos;
    }

    public InspectionReport inspect(Path subjectFolder) {
        Diagnostics diagnostics = new Diagnostics();
        Optional<Course> course = SubjectLoader.load(subjectFolder, diagnostics);
        course.ifPresent(found -> validator.validate(new ValidationContext(
                found, new FolderSourceDocuments(subjectFolder.resolve(found.subject().sourcesDir())), videos), diagnostics));
        return new InspectionReport(course, diagnostics.list().stream().sorted(BY_PLACE).toList());
    }
}
