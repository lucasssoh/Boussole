package io.github.lucasssoh.boussole.server.guide;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * The tracks shipped inside Boussole (repository folder {@code guide/<track>/<language>}): the
 * guide a new Boussole opens on, and the tracks to learn more, such as writing a track by hand.
 * They are copied out of the jar at each start, so that they follow the version of Boussole, in
 * every language they are written in, and always shown apart from the learner's own tracks.
 */
public final class BundledTracks {

    /** The ID of the guide a new Boussole opens on, shown beside the session to resume. */
    public static final String STARTER = "boussole-guide";

    private static final String ROOT = "boussole-guide/";
    private static final Pattern TRACK_AND_PATH = Pattern.compile(Pattern.quote(ROOT) + "([^/]+)/([a-zA-Z-]+)/(.+)$");
    private static final Pattern ID = Pattern.compile("(?m)^id:\\s*(\\S+)\\s*$");

    private BundledTracks() {
    }

    /**
     * Copies every track, in every language, into {@code folder} ({@code <track>/<language>}),
     * replacing older copies, and returns the versions, the starter guide first.
     */
    public static List<BuiltInTrack> install(Path folder) {
        try {
            Map<String, List<Resource>> byVersion = new TreeMap<>();
            for (Resource file : new PathMatchingResourcePatternResolver().getResources("classpath*:" + ROOT + "*/*/**/*.*")) {
                Matcher matcher = TRACK_AND_PATH.matcher(file.getURL().toString());
                if (matcher.find()) {
                    byVersion.computeIfAbsent(matcher.group(1) + "/" + matcher.group(2), unused -> new ArrayList<>()).add(file);
                }
            }
            deleteTree(folder);
            List<BuiltInTrack> tracks = new ArrayList<>();
            for (Map.Entry<String, List<Resource>> track : byVersion.entrySet()) {
                Path target = folder.resolve(track.getKey());
                for (Resource file : track.getValue()) {
                    Matcher matcher = TRACK_AND_PATH.matcher(file.getURL().toString());
                    matcher.find();
                    Path destination = target.resolve(matcher.group(3));
                    Files.createDirectories(destination.getParent());
                    try (InputStream in = file.getInputStream()) {
                        Files.copy(in, destination, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
                tracks.add(new BuiltInTrack(idOf(target), target));
            }
            tracks.sort(Comparator.comparing((BuiltInTrack track) -> !track.id().equals(STARTER)));
            return List.copyOf(tracks);
        } catch (IOException e) {
            throw new UncheckedIOException("cannot install the built-in tracks in " + folder, e);
        }
    }

    private static String idOf(Path track) throws IOException {
        Matcher matcher = ID.matcher(Files.readString(track.resolve("subject.yaml")));
        if (!matcher.find()) {
            throw new IllegalStateException("the built-in track " + track + " has no id in subject.yaml");
        }
        return matcher.group(1);
    }

    private static void deleteTree(Path folder) throws IOException {
        if (!Files.exists(folder)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(folder)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.delete(path);
            }
        }
    }
}
