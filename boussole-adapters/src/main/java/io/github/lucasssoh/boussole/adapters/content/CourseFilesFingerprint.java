package io.github.lucasssoh.boussole.adapters.content;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;

/** A number that changes when a course file of a subject folder is added, removed or modified. */
final class CourseFilesFingerprint {

    private static final String[] TOP_FILES = {"subject.yaml", "learner.yaml"};
    private static final String COURSE_DIR = "cursus";

    private CourseFilesFingerprint() {
    }

    static long of(Path folder) {
        try (Stream<Path> course = Files.isDirectory(folder.resolve(COURSE_DIR)) ? Files.walk(folder.resolve(COURSE_DIR)) : Stream.empty()) {
            return Stream.concat(Arrays.stream(TOP_FILES).map(folder::resolve), course)
                    .filter(Files::isRegularFile)
                    .mapToLong(CourseFilesFingerprint::stamp)
                    .reduce(17, (hash, stamp) -> 31 * hash + stamp);
        } catch (IOException | UncheckedIOException e) {
            return 0;
        }
    }

    private static long stamp(Path file) {
        try {
            return file.hashCode() * 31L + Files.getLastModifiedTime(file).toMillis() * 7 + Files.size(file);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
