package io.github.lucasssoh.boussole.format.load;

import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCode;
import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCollector;
import io.github.lucasssoh.boussole.format.model.LearnerProfile;
import io.github.lucasssoh.boussole.format.model.LearnerProfile.Closing;
import io.github.lucasssoh.boussole.format.model.LearnerProfile.Explanations;
import io.github.lucasssoh.boussole.format.model.LearnerProfile.Goal;
import io.github.lucasssoh.boussole.format.model.LearnerProfile.Order;
import io.github.lucasssoh.boussole.format.model.LearnerProfile.Videos;
import io.github.lucasssoh.boussole.format.model.SessionKind;
import io.github.lucasssoh.boussole.format.yaml.YamlMap;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/** Reads {@code learner.yaml}, which a subject may not have. Missing fields take their default. */
final class LearnerProfileReader {

    static final int DEFAULT_MINUTES = 60;
    private static final Set<String> FIELDS = Set.of(
            "background", "goal", "session_minutes", "explanations", "order", "quick_check", "closing", "videos", "notes");

    private LearnerProfileReader() {
    }

    static Optional<LearnerProfile> read(SubjectFolderFiles files, DiagnosticCollector diagnostics) {
        if (!files.exists(SubjectFolderFiles.LEARNER)) {
            return Optional.empty();
        }
        return files.read(SubjectFolderFiles.LEARNER)
                .flatMap(text -> YamlMap.parse(text, YamlMap.YamlSource.of(SubjectFolderFiles.LEARNER, diagnostics)))
                .map(map -> profile(map, diagnostics));
    }

    private static LearnerProfile profile(YamlMap map, DiagnosticCollector diagnostics) {
        map.reportUnknownKeys(FIELDS);
        int minutes = map.integer("session_minutes").orElse(DEFAULT_MINUTES);
        if (minutes < SessionKind.LESSON.minDuration() || minutes > SessionKind.LESSON.maxDuration()) {
            diagnostics.report(DiagnosticCode.F001, map.positionOf("session_minutes"), "`session_minutes` is between "
                    + SessionKind.LESSON.minDuration() + " and " + SessionKind.LESSON.maxDuration() + ", not " + minutes);
            minutes = DEFAULT_MINUTES;
        }
        return new LearnerProfile(
                map.text("background").orElse("").strip(),
                choice(map, "goal", Goal.class, Goal.GOOD, diagnostics),
                minutes,
                choice(map, "explanations", Explanations.class, Explanations.CONCISE, diagnostics),
                choice(map, "order", Order.class, Order.EXAMPLE_FIRST, diagnostics),
                map.flag("quick_check").orElse(false),
                choice(map, "closing", Closing.class, Closing.QUIZ, diagnostics),
                choice(map, "videos", Videos.class, Videos.SOME, diagnostics),
                map.text("notes").orElse("").strip(),
                map.position());
    }

    /** A value written in lowercase with dashes, {@code try-first} for {@code TRY_FIRST}. */
    private static <E extends Enum<E>> E choice(YamlMap map, String key, Class<E> type, E fallback, DiagnosticCollector diagnostics) {
        Optional<String> value = map.text(key);
        if (value.isEmpty()) {
            return fallback;
        }
        String name = value.get().strip().toUpperCase(Locale.ROOT).replace('-', '_');
        return Arrays.stream(type.getEnumConstants()).filter(constant -> constant.name().equals(name)).findFirst()
                .orElseGet(() -> {
                    diagnostics.report(DiagnosticCode.F001, map.positionOf(key), "`" + key + "` is one of "
                            + Arrays.stream(type.getEnumConstants()).map(LearnerProfileReader::written).collect(Collectors.joining(", "))
                            + ", not `" + value.get() + "`");
                    return fallback;
                });
    }

    private static String written(Enum<?> constant) {
        return constant.name().toLowerCase(Locale.ROOT).replace('_', '-');
    }
}
