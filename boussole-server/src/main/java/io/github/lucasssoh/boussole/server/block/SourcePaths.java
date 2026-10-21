package io.github.lucasssoh.boussole.server.block;

import java.nio.file.Path;

/**
 * Paths of source documents, relative to the subject folder, as the interface asks for them. When
 * the sources folder lies outside the subject folder (a version of a track in another language
 * reading the original's documents), the path starts with {@link #SOURCES}: a browser would
 * rewrite a path holding {@code ..} before sending it.
 */
public final class SourcePaths {

    /** The start of a path read in the sources folder rather than in the subject folder. */
    public static final String SOURCES = "@sources";

    private SourcePaths() {
    }

    public static String of(String sourcesDir, String file) {
        Path inSubject = Path.of(sourcesDir).resolve(file).normalize();
        Path path = inSubject.startsWith("..") ? Path.of(SOURCES).resolve(Path.of(file).normalize()) : inSubject;
        return path.toString().replace('\\', '/');
    }
}
