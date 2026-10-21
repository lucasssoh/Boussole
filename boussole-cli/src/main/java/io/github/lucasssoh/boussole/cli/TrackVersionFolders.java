package io.github.lucasssoh.boussole.cli;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Where the other versions of a track are: the folders next to it ({@code python/fr} beside
 * {@code python/en}) and the subjects of config.yaml whose {@code subject.yaml} has the same ID.
 */
final class TrackVersionFolders {

    private static final Pattern ID = Pattern.compile("(?m)^id:\\s*[\"']?([^\\s\"']+)");

    private TrackVersionFolders() {
    }

    static List<Path> of(Path folder, String id, List<Path> subjects) {
        Path self = normal(folder);
        Stream<Path> siblings = Stream.empty();
        Path parent = self.getParent();
        if (parent != null) {
            try (Stream<Path> listed = Files.list(parent)) {
                siblings = listed.filter(Files::isDirectory).toList().stream();
            } catch (IOException e) {
                siblings = Stream.empty();
            }
        }
        return Stream.concat(siblings, subjects.stream())
                .map(TrackVersionFolders::normal)
                .filter(candidate -> !candidate.equals(self))
                .distinct()
                .filter(candidate -> id.equals(idOf(candidate)))
                .toList();
    }

    private static String idOf(Path folder) {
        Path subject = folder.resolve("subject.yaml");
        if (!Files.isRegularFile(subject)) {
            return null;
        }
        try {
            Matcher matcher = ID.matcher(Files.readString(subject));
            return matcher.find() ? matcher.group(1) : null;
        } catch (IOException e) {
            return null;
        }
    }

    private static Path normal(Path path) {
        return Objects.requireNonNull(path).toAbsolutePath().normalize();
    }
}
