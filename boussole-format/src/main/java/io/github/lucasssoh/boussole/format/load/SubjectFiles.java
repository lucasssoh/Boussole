package io.github.lucasssoh.boussole.format.load;

import io.github.lucasssoh.boussole.format.diagnostic.Code;
import io.github.lucasssoh.boussole.format.diagnostic.Diagnostics;
import io.github.lucasssoh.boussole.format.diagnostic.Position;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/** The files of a subject folder, named relative to it with {@code /}. */
final class SubjectFiles {

    static final String SUBJECT = "subject.yaml";
    static final String SYLLABUS = "cursus/syllabus.yaml";
    static final String LEXICON = "cursus/lexique.yaml";

    private final Path root;
    private final Diagnostics diagnostics;

    SubjectFiles(Path root, Diagnostics diagnostics) {
        this.root = root;
        this.diagnostics = diagnostics;
    }

    Path root() {
        return root;
    }

    boolean exists(String name) {
        return Files.isRegularFile(root.resolve(name));
    }

    /** The text of a file, or empty after reporting why it cannot be read. */
    Optional<String> read(String name) {
        Path path = root.resolve(name);
        if (!Files.isRegularFile(path)) {
            diagnostics.report(Code.F001, Position.fileOnly(name), "the file is missing");
            return Optional.empty();
        }
        try {
            return Optional.of(Files.readString(path, StandardCharsets.UTF_8));
        } catch (IOException e) {
            diagnostics.report(Code.F001, Position.fileOnly(name), "the file cannot be read: " + e.getMessage());
            return Optional.empty();
        }
    }
}
