package io.github.lucasssoh.boussole.cli.serve;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/** The process ID of the running server, written by {@code serve} and removed when it ends. */
final class ServerPidFile {

    private final Path file;

    ServerPidFile(Path file) {
        this.file = file;
    }

    void writeCurrent() {
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, String.valueOf(ProcessHandle.current().pid()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        Runtime.getRuntime().addShutdownHook(new Thread(this::delete));
    }

    /** The server process, if the file names one still running as {@code boussole … serve}. */
    Optional<ProcessHandle> server() {
        try {
            long pid = Long.parseLong(Files.readString(file).strip());
            return ProcessHandle.of(pid)
                    .filter(ProcessHandle::isAlive)
                    .filter(process -> process.info().commandLine().filter(line -> line.contains("serve")).isPresent());
        } catch (IOException | NumberFormatException e) {
            return Optional.empty();
        }
    }

    void delete() {
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            // Nothing left to clean.
        }
    }
}
