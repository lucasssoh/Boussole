package io.github.lucasssoh.boussole.server.block;

import java.nio.file.Path;

/** Paths of source documents, relative to the subject folder, as the interface asks for them. */
public final class SourcePaths {

    private SourcePaths() {
    }

    public static String of(String sourcesDir, String file) {
        return Path.of(sourcesDir).resolve(file).normalize().toString().replace('\\', '/');
    }
}
