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
import java.util.Locale;
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
 * English or in French, and always shown apart from the learner's own tracks.
 */
public final class BundledTracks {

    /** The ID of the guide a new Boussole opens on, shown beside the session to resume. */
    public static final String STARTER = "boussole-guide";

    private static final String ROOT = "boussole-guide/";
    private static final List<String> LANGUAGES = List.of("en", "fr");
    private static final Pattern TRACK_AND_PATH = Pattern.compile(Pattern.quote(ROOT) + "([^/]+)/([a-z]{2})/(.+)$");
    private static final Pattern ID = Pattern.compile("(?m)^id:\\s*(\\S+)\\s*$");

    private BundledTracks() {
    }

    /** The language of the tracks: the one asked for if they have it, else English. */
    public static String languageFor(String wanted) {
        String language = wanted.toLowerCase(Locale.ROOT);
        return LANGUAGES.stream().filter(language::startsWith).findFirst().orElse("en");
    }

    /**
     * Copies every track in {@code language} into {@code folder}, replacing older copies, and
     * returns them, the starter guide first.
     */
    public static List<BuiltInTrack> install(Path folder, String language) {
        String lang = languageFor(language);
        try {
            Map<String, List<Resource>> byTrack = new TreeMap<>();
            for (Resource file : new PathMatchingResourcePatternResolver().getResources("classpath*:" + ROOT + "*/" + lang + "/**/*.*")) {
                Matcher matcher = TRACK_AND_PATH.matcher(file.getURL().toString());
                if (matcher.find()) {
                    byTrack.computeIfAbsent(matcher.group(1), unused -> new ArrayList<>()).add(file);
                }
            }
            List<BuiltInTrack> tracks = new ArrayList<>();
            for (Map.Entry<String, List<Resource>> track : byTrack.entrySet()) {
                Path target = folder.resolve(track.getKey()).resolve(lang);
                deleteTree(target);
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
            tracks.sort(Comparator.comparing((BuiltInTrack track) -> !track.id().equals(STARTER)).thenComparing(BuiltInTrack::id));
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
