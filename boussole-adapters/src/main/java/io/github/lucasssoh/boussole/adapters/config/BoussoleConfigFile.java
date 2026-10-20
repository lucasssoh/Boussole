package io.github.lucasssoh.boussole.adapters.config;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.exceptions.YamlEngineException;

/**
 * Reads {@code config.yaml}, and adds a subject folder to it while keeping the rest of the file,
 * comments included, as the learner wrote it.
 */
public final class BoussoleConfigFile {

    private static final String SUBJECTS = "subjects:";

    private final Path file;
    private final Path home;

    public BoussoleConfigFile(Path file, Path home) {
        this.file = file;
        this.home = home;
    }

    public static BoussoleConfigFile forCurrentUser() {
        return new BoussoleConfigFile(BoussolePaths.forCurrentUser().configFile(), Path.of(System.getProperty("user.home")));
    }

    public Path path() {
        return file;
    }

    public BoussoleConfig read() {
        if (!Files.exists(file)) {
            return BoussoleConfig.defaults();
        }
        Object loaded = load(text());
        if (!(loaded instanceof Map<?, ?> map)) {
            return BoussoleConfig.defaults();
        }
        return new BoussoleConfig(
                Optional.ofNullable(map.get("language")).map(String::valueOf),
                map.get("port") instanceof Integer port ? port : BoussoleConfig.DEFAULT_PORT,
                map.get("subjects") instanceof List<?> folders
                        ? folders.stream().map(String::valueOf).map(this::expand).toList()
                        : List.of());
    }

    /** Adds the folder to the subjects, unless it is there already. Says whether it was added. */
    public boolean addSubject(Path folder) {
        Path absolute = folder.toAbsolutePath().normalize();
        if (read().subjects().contains(absolute)) {
            return false;
        }
        String item = "  - " + shortened(absolute);
        List<String> lines = Files.exists(file) ? new ArrayList<>(text().lines().toList()) : new ArrayList<>();
        int key = indexOfSubjectsKey(lines);
        if (key < 0) {
            lines.add(SUBJECTS);
            lines.add(item);
        } else {
            lines.set(key, SUBJECTS);
            int last = key;
            while (last + 1 < lines.size() && isListItemOrBlank(lines.get(last + 1))) {
                last++;
            }
            while (last > key && lines.get(last).isBlank()) {
                last--;
            }
            lines.add(last + 1, item);
        }
        write(String.join("\n", lines) + "\n");
        return true;
    }

    private static int indexOfSubjectsKey(List<String> lines) {
        for (int index = 0; index < lines.size(); index++) {
            if (lines.get(index).startsWith(SUBJECTS)) {
                return index;
            }
        }
        return -1;
    }

    private static boolean isListItemOrBlank(String line) {
        return line.isBlank() || line.stripLeading().startsWith("- ") && Character.isWhitespace(line.charAt(0));
    }

    private Path expand(String folder) {
        return (folder.equals("~") || folder.startsWith("~/") ? home.resolve(folder.substring(Math.min(2, folder.length()))) : Path.of(folder))
                .toAbsolutePath()
                .normalize();
    }

    private String shortened(Path folder) {
        return folder.startsWith(home) && !folder.equals(home) ? "~/" + home.relativize(folder) : folder.toString();
    }

    private static Object load(String text) {
        try {
            return new Load(LoadSettings.builder().build()).loadFromString(text);
        } catch (YamlEngineException e) {
            throw new IllegalStateException("config.yaml is not valid YAML: " + e.getMessage(), e);
        }
    }

    private String text() {
        try {
            return Files.readString(file);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void write(String text) {
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, text);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
