package io.github.lucasssoh.boussole.server.course;

import io.github.lucasssoh.boussole.core.learn.CourseOverview;
import io.github.lucasssoh.boussole.format.model.Course;
import io.github.lucasssoh.boussole.format.model.LexiconTerm;
import io.github.lucasssoh.boussole.format.model.SyllabusChapter;
import io.github.lucasssoh.boussole.format.model.SyllabusSession;
import java.util.List;
import io.github.lucasssoh.boussole.core.learn.progress.SessionProgress;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A subject, in the language it is followed in ({@code lang}) among those it exists in: its
 * chapters and sessions, what is written and finished, and its lexicon.
 */
record CourseView(
        String id,
        String name,
        String lang,
        List<String> languages,
        String description,
        String sourcesDir,
        List<ChapterView> chapters,
        List<TermView> lexicon,
        ResumeView resume) {

    static CourseView of(CourseOverview overview) {
        Course course = overview.course();
        Set<String> written = course.sessions().stream().map(found -> found.entry().id()).collect(Collectors.toSet());
        return new CourseView(
                course.subject().id(),
                course.subject().name(),
                course.subject().lang(),
                overview.languages(),
                course.subject().description().orElse(null),
                course.subject().sourcesDir(),
                course.syllabus().chapters().stream()
                        .map(chapter -> ChapterView.of(chapter, written, overview.finishedSessions(), overview.sessionProgress()))
                        .toList(),
                course.lexicon().terms().stream().map(TermView::of).toList(),
                ResumeView.of(overview).orElse(null));
    }

    record ChapterView(String id, String title, List<SessionEntryView> sessions) {

        static ChapterView of(
                SyllabusChapter chapter, Set<String> written, Set<String> finished, Map<String, SessionProgress> progress) {
            return new ChapterView(chapter.id(), chapter.title(), chapter.sessions().stream()
                    .map(entry -> SessionEntryView.of(entry, written.contains(entry.id()), finished.contains(entry.id()),
                            progress.getOrDefault(entry.id(), new SessionProgress(0, 0, 0))))
                    .toList());
        }
    }

    /** {@code questions}, {@code answered}, {@code right}: how far the learner is in the session. */
    record SessionEntryView(
            String id,
            String title,
            String kind,
            int duration,
            String goal,
            boolean written,
            boolean finished,
            int questions,
            int answered,
            int right) {

        static SessionEntryView of(SyllabusSession entry, boolean written, boolean finished, SessionProgress progress) {
            return new SessionEntryView(entry.id(), entry.title(), entry.kind().name().toLowerCase(Locale.ROOT),
                    entry.duration(), entry.goal(), written, finished, progress.questions(), progress.answered(), progress.right());
        }
    }

    record TermView(String id, String term, List<String> aliases, String definition, List<String> see) {

        static TermView of(LexiconTerm term) {
            return new TermView(term.id(), term.term(), term.aliases(), term.definition(), term.see());
        }
    }
}
