package io.github.lucasssoh.boussole.format.version;

import io.github.lucasssoh.boussole.format.diagnostic.Diagnostic;
import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCode;
import io.github.lucasssoh.boussole.format.diagnostic.SourcePosition;
import io.github.lucasssoh.boussole.format.model.Course;
import io.github.lucasssoh.boussole.format.model.CourseSession;
import io.github.lucasssoh.boussole.format.model.SyllabusSession;
import io.github.lucasssoh.boussole.format.model.answer.FillAnswer;
import io.github.lucasssoh.boussole.format.model.answer.FillGap;
import io.github.lucasssoh.boussole.format.model.answer.MatchAnswer;
import io.github.lucasssoh.boussole.format.model.answer.OrderAnswer;
import io.github.lucasssoh.boussole.format.model.block.Block;
import io.github.lucasssoh.boussole.format.model.block.ExerciseBlock;
import io.github.lucasssoh.boussole.format.model.block.ExerciseRefBlock;
import io.github.lucasssoh.boussole.format.model.block.McqBlock;
import io.github.lucasssoh.boussole.format.model.part.NotionPart;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * Compares two versions of a track, the same subject ID in two languages: only the text is
 * translated, so they have the same sessions, notions and questions, with the same IDs, and each
 * question asks the same thing (its kind, its number of choices and which are right, its gaps).
 * The differences are reported in the files of {@code version}.
 */
public final class TrackVersions {

    private TrackVersions() {
    }

    public static List<Diagnostic> compare(Course reference, Course version) {
        List<Diagnostic> found = new ArrayList<>();
        String other = "the `" + reference.subject().lang() + "` version";
        if (reference.subject().lang().equalsIgnoreCase(version.subject().lang())) {
            found.add(new Diagnostic(DiagnosticCode.F040, version.subject().position(), "`" + reference.root() + "` is a version of `"
                    + version.subject().id() + "` in the same language, `" + version.subject().lang() + "`: give each version its own `lang`"));
            return found;
        }
        List<String> planned = plannedSessions(reference);
        if (!planned.equals(plannedSessions(version))) {
            found.add(new Diagnostic(DiagnosticCode.F040, version.subject().position(), "the plan lists the sessions "
                    + plannedSessions(version) + ", " + other + " " + planned + ": the same sessions, with the same IDs, in every language"));
        }
        Map<String, CourseSession> sessions = byId(version);
        byId(reference).forEach((id, session) -> {
            CourseSession translated = sessions.get(id);
            if (translated == null) {
                if (planned.contains(id)) {
                    found.add(new Diagnostic(DiagnosticCode.F041, version.subject().position(),
                            "session `" + id + "` is written in " + other + ", not yet in this one"));
                }
            } else {
                compareSessions(session, translated, other, found);
            }
        });
        return found;
    }

    private static void compareSessions(CourseSession reference, CourseSession version, String other, List<Diagnostic> found) {
        SourcePosition file = SourcePosition.fileOnly(version.file());
        List<String> notions = notions(reference);
        if (!notions.equals(notions(version))) {
            found.add(new Diagnostic(DiagnosticCode.F040, file, "session `" + version.entry().id() + "` has the notions " + notions(version)
                    + ", " + other + " " + notions + ": translate the titles, keep the IDs `{#…}`"));
        }
        Map<String, Block> questions = questions(version);
        Map<String, Block> expected = questions(reference);
        if (!List.copyOf(expected.keySet()).equals(List.copyOf(questions.keySet()))) {
            found.add(new Diagnostic(DiagnosticCode.F040, file, "session `" + version.entry().id() + "` asks the questions "
                    + questions.keySet() + ", " + other + " " + expected.keySet() + ": the same questions, with the same IDs, in the same order"));
            return;
        }
        expected.forEach((id, question) -> {
            String shape = shape(question);
            Block translated = questions.get(id);
            if (!shape.equals(shape(translated))) {
                found.add(new Diagnostic(DiagnosticCode.F040, positionOf(translated, file), "question `" + id + "` is "
                        + shape(translated) + ", in " + other + " " + shape + ": a translation asks the same thing"));
            }
        });
    }

    private static List<String> plannedSessions(Course course) {
        return course.syllabus().chapters().stream()
                .flatMap(chapter -> chapter.sessions().stream())
                .map(SyllabusSession::id)
                .toList();
    }

    private static Map<String, CourseSession> byId(Course course) {
        Map<String, CourseSession> sessions = new LinkedHashMap<>();
        course.sessions().forEach(session -> sessions.put(session.entry().id(), session));
        return sessions;
    }

    private static List<String> notions(CourseSession session) {
        return session.session().parts().stream()
                .filter(NotionPart.class::isInstance)
                .map(part -> ((NotionPart) part).id())
                .toList();
    }

    /** The questions of a session in order, by ID: written in it, or asked again with {@code ::ref}. */
    private static Map<String, Block> questions(CourseSession session) {
        Map<String, Block> questions = new LinkedHashMap<>();
        session.session().blocks().forEach(block -> {
            switch (block) {
                case McqBlock mcq -> questions.put(mcq.id(), mcq);
                case ExerciseBlock exercise -> questions.put(exercise.id(), exercise);
                case ExerciseRefBlock ref -> questions.put(ref.id(), ref);
                default -> {
                }
            }
        });
        return questions;
    }

    /** What a question asks, apart from its words. */
    private static String shape(Block question) {
        return switch (question) {
            case McqBlock mcq -> "an MCQ of " + mcq.choices().size() + " choices, right: " + IntStream.range(0, mcq.choices().size())
                    .filter(index -> mcq.choices().get(index).correct()).mapToObj(index -> String.valueOf(index + 1)).toList();
            case ExerciseBlock exercise -> "a `" + exercise.type().attributeValue() + "` exercise" + exercise.answer().map(answer -> switch (answer) {
                case FillAnswer fill -> " with the gaps " + fill.gaps().stream().map(TrackVersions::gap).toList();
                case OrderAnswer order -> " of " + order.items().size() + " items";
                case MatchAnswer match -> " of " + match.pairs().size() + " pairs";
                default -> "";
            }).orElse("");
            case ExerciseRefBlock ref -> "the question `" + ref.id() + "` asked again";
            default -> "a block";
        };
    }

    private static String gap(FillGap gap) {
        return gap.number() + " " + gap.expected().getClass().getSimpleName().replace("Expectation", "").toLowerCase(java.util.Locale.ROOT);
    }

    private static SourcePosition positionOf(Block block, SourcePosition fallback) {
        return Optional.ofNullable(block.position()).orElse(fallback);
    }
}
