package io.github.lucasssoh.boussole.server.guide;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * The guide to Boussole, a subject shipped inside it (repository folder {@code guide/}), shown
 * while the learner has no subject of their own. It is copied out of the jar at each start, so
 * that it follows the version of Boussole, in English or in French.
 */
public final class BundledGuide {

    private static final String ROOT = "boussole-guide/";
    private static final List<String> LANGUAGES = List.of("en", "fr");

    private BundledGuide() {
    }

    /** The language of the guide: the one asked for if the guide has it, else English. */
    public static String languageFor(String wanted) {
        String language = wanted.toLowerCase(Locale.ROOT);
        return LANGUAGES.stream().filter(language::startsWith).findFirst().orElse("en");
    }

    /** Copies the guide in {@code language} into {@code folder}, replacing an older copy, and returns its subject folder. */
    public static Path install(Path folder, String language) {
        String lang = languageFor(language);
        Path target = folder.resolve(lang);
        try {
            deleteTree(target);
            String prefix = ROOT + lang + "/";
            Resource[] files = new PathMatchingResourcePatternResolver().getResources("classpath*:" + prefix + "**/*.*");
            for (Resource file : files) {
                String url = file.getURL().toString();
                Path destination = target.resolve(url.substring(url.lastIndexOf(prefix) + prefix.length()));
                Files.createDirectories(destination.getParent());
                try (InputStream in = file.getInputStream()) {
                    Files.copy(in, destination, StandardCopyOption.REPLACE_EXISTING);
                }
            }
            return target;
        } catch (IOException e) {
            throw new UncheckedIOException("cannot install the guide in " + target, e);
        }
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
