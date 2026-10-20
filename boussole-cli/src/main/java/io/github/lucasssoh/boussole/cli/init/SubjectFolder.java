package io.github.lucasssoh.boussole.cli.init;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

/** The files of the subject folder being initialised. */
final class SubjectFolder {

    private final Path root;

    SubjectFolder(Path root) {
        this.root = root;
    }

    Path root() {
        return root;
    }

    boolean exists(String name) {
        return Files.exists(root.resolve(name));
    }

    Optional<String> read(String name) {
        Path path = root.resolve(name);
        if (!Files.isRegularFile(path)) {
            return Optional.empty();
        }
        try {
            return Optional.of(Files.readString(path, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    void write(String name, String content) {
        try {
            Path path = root.resolve(name);
            Files.createDirectories(path.getParent());
            Files.writeString(path, content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Whether the folder itself holds PDF documents. */
    boolean holdsPdfs() {
        try (Stream<Path> files = Files.list(root)) {
            return files.anyMatch(file -> Files.isRegularFile(file) && file.getFileName().toString().toLowerCase().endsWith(".pdf"));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
