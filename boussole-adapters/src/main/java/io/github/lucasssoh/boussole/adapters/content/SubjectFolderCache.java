package io.github.lucasssoh.boussole.adapters.content;

import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCollector;
import io.github.lucasssoh.boussole.format.load.CourseLoader;
import io.github.lucasssoh.boussole.format.model.Course;
import java.nio.file.Path;
import java.util.Optional;

/** The course of one folder, read again when its {@link CourseFilesFingerprint} changes. */
final class SubjectFolderCache {

    private final Path folder;
    private long fingerprint;
    private Optional<Course> course = Optional.empty();

    SubjectFolderCache(Path folder) {
        this.folder = folder;
    }

    synchronized Optional<Course> course() {
        long current = CourseFilesFingerprint.of(folder);
        if (current != fingerprint || course.isEmpty()) {
            course = CourseLoader.load(folder, new DiagnosticCollector());
            fingerprint = current;
        }
        return course;
    }
}
