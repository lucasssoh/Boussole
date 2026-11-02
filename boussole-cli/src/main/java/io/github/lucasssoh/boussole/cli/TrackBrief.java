package io.github.lucasssoh.boussole.cli;

import io.github.lucasssoh.boussole.format.model.Course;
import io.github.lucasssoh.boussole.format.model.CourseSession;
import io.github.lucasssoh.boussole.format.model.LearnerProfile;
import io.github.lucasssoh.boussole.format.model.LexiconTerm;
import io.github.lucasssoh.boussole.format.model.SourceReference;
import io.github.lucasssoh.boussole.format.model.SyllabusChapter;
import io.github.lucasssoh.boussole.format.model.SyllabusSession;
import io.github.lucasssoh.boussole.format.model.part.NotionPart;
import io.github.lucasssoh.boussole.format.source.FolderSourceDocuments;
import io.github.lucasssoh.boussole.format.source.SourceInventory;
import io.github.lucasssoh.boussole.format.source.SourceInventory.SourceState;
import io.github.lucasssoh.boussole.format.source.SourceInventory.State;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Where a track stands, in a few lines an agent reads instead of the whole track: the documents to
 * take in, each chapter with its documents and its sessions (written or not, the notions of those
 * written), the session IDs taken, the terms of the lexicon and the learner's profile. Boussole
 * computes it from the files, so an agent that continues a track starts from it rather than
 * reading every session again.
 */
public final class TrackBrief {

    private TrackBrief() {
    }

    /** The documents that ask for an agent: new, changed, gone. Pending ones wait by choice. */
    public static List<SourceState> toTakeIn(List<SourceState> states) {
        return states.stream()
                .filter(state -> state.state() == State.NEW || state.state() == State.CHANGED || state.state() == State.GONE)
                .toList();
    }

    /** The documents of the sources folder of {@code course}, compared with {@code cursus/sources.yaml}. */
    public static List<SourceState> documents(Path folder, Course course) {
        return SourceInventory.compare(course.sources(),
                new FolderSourceDocuments(folder.resolve(course.subject().sourcesDir()), folder));
    }

    public static String of(Course course, List<SourceState> states) {
        StringBuilder out = new StringBuilder();
        out.append("Track ").append(course.subject().id()).append(" (").append(course.subject().name()).append("), lang ")
                .append(course.subject().lang()).append(", sources in ").append(course.subject().sourcesDir())
                .append(", plan ").append(course.syllabus().status().name().toLowerCase(Locale.ROOT)).append(".\n");
        course.learner().ifPresent(learner -> out.append("Learner: ").append(learner(learner)).append(".\n"));

        List<SourceState> toTakeIn = toTakeIn(states);
        out.append("\nDocuments to take in: ").append(toTakeIn.isEmpty() ? "none" : toTakeIn.size()).append('\n');
        toTakeIn.forEach(state -> out.append("  ").append(String.format(Locale.ROOT, "%-8s", name(state.state())))
                .append(state.file())
                .append(state.sha256().map(sha -> "  sha256: " + sha).orElse(""))
                .append(state.entry().filter(entry -> !entry.chapters().isEmpty())
                        .map(entry -> "  chapters: " + String.join(", ", entry.chapters())).orElse(""))
                .append('\n'));
        List<SourceState> pending = states.stream().filter(state -> state.state() == State.PENDING).toList();
        if (!pending.isEmpty()) {
            out.append("Pending, by choice: ")
                    .append(pending.stream().map(SourceState::file).collect(Collectors.joining(", "))).append('\n');
        }

        out.append("\nChapters, in the order of the plan:\n");
        for (SyllabusChapter chapter : course.syllabus().chapters()) {
            out.append("  ").append(chapter.id()).append("  \"").append(chapter.title()).append("\"  cursus/")
                    .append(chapter.dir()).append('\n');
            if (!chapter.sources().isEmpty()) {
                out.append("    documents: ").append(chapter.sources().stream().map(TrackBrief::reference)
                        .collect(Collectors.joining(", "))).append('\n');
            }
            for (SyllabusSession entry : chapter.sessions()) {
                Optional<CourseSession> written = course.sessions().stream()
                        .filter(session -> session.chapter().id().equals(chapter.id()) && session.entry().id().equals(entry.id()))
                        .findFirst();
                out.append(String.format(Locale.ROOT, "    %-6s %-6s %3d min  %-8s %s%n", entry.id(),
                        entry.kind().name().toLowerCase(Locale.ROOT), entry.duration(), written.isPresent() ? "written" : "to write",
                        entry.title()));
                written.map(TrackBrief::notions).filter(notions -> !notions.isEmpty())
                        .ifPresent(notions -> out.append("           notions: ").append(notions).append('\n'));
            }
        }

        List<LexiconTerm> terms = course.lexicon().terms();
        out.append("\nLexicon: ").append(terms.isEmpty() ? "empty"
                : terms.size() + " terms: " + terms.stream().map(LexiconTerm::term).collect(Collectors.joining(", "))).append('\n');

        out.append("""

                Continue from here: read the documents to take in; to match the notation and the tone,
                one written session of the chapter you extend, no more; for warmup and review questions,
                `boussole index .`. New sessions take IDs not listed above; IDs above never change.
                """);
        return out.toString();
    }

    private static String learner(LearnerProfile learner) {
        return String.join(", ",
                "goal " + lower(learner.goal()),
                learner.sessionMinutes() + " min sessions",
                lower(learner.explanations()) + " explanations",
                lower(learner.order()),
                learner.quickCheck() ? "quick check" : "no quick check",
                "closing " + lower(learner.closing()),
                "videos " + lower(learner.videos()));
    }

    private static String notions(CourseSession session) {
        return session.session().parts().stream()
                .filter(NotionPart.class::isInstance)
                .map(part -> ((NotionPart) part).title())
                .collect(Collectors.joining("; "));
    }

    private static String reference(SourceReference reference) {
        return reference.file() + (reference.page().isPresent() ? " p. " + reference.page().getAsInt() : "");
    }

    private static String name(State state) {
        return lower(state);
    }

    private static String lower(Enum<?> value) {
        return value.name().toLowerCase(Locale.ROOT).replace('_', ' ');
    }
}
