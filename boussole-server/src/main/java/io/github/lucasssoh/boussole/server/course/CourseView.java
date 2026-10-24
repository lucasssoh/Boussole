package io.github.lucasssoh.boussole.server.course;

import io.github.lucasssoh.boussole.core.learn.CourseOverview;
import io.github.lucasssoh.boussole.format.model.Course;
import io.github.lucasssoh.boussole.format.model.Domain;
import io.github.lucasssoh.boussole.format.model.LexiconTerm;
import io.github.lucasssoh.boussole.format.model.TrackLevel;
import io.github.lucasssoh.boussole.format.model.SessionId;
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
 * chapters and sessions, what is written and finished, its lexicon, who makes it and with what,
 * how demanding it is, its field, and the points earned, missed for good, out of all it gives.
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
        ResumeView resume,
        List<CreditViews.CreditView> authors,
        List<CreditViews.CreditView> contributors,
        List<CreditViews.WriterView> writtenWith,
        String level,
        String domain,
        ScoreView score) {

    static CourseView of(CourseOverview overview) {
        Course course = overview.course();
        Set<String> written = course.sessions().stream().map(found -> found.entry().id()).collect(Collectors.toSet());
        Map<String, String> titles = course.syllabus().chapters().stream().flatMap(chapter -> chapter.sessions().stream())
                .collect(Collectors.toMap(SyllabusSession::id, SyllabusSession::title, (first, second) -> first));
        return new CourseView(
                course.subject().id(),
                course.subject().name(),
                course.subject().lang(),
                overview.languages(),
                course.subject().description().orElse(null),
                course.subject().sourcesDir(),
                course.syllabus().chapters().stream()
                        .map(chapter -> ChapterView.of(chapter, written, overview, titles))
                        .toList(),
                course.lexicon().terms().stream().map(TermView::of).toList(),
                ResumeView.of(overview).orElse(null),
                CreditViews.credits(course.subject().authors()),
                CreditViews.credits(course.subject().contributors()),
                CreditViews.writers(course),
                course.subject().level().map(TrackLevel::id).orElse(null),
                course.subject().domain().map(Domain::id).orElse(null),
                new ScoreView(overview.score().earned(), overview.score().missed(), overview.score().possible()));
    }

    record ChapterView(String id, String title, List<SessionEntryView> sessions) {

        static ChapterView of(SyllabusChapter chapter, Set<String> written, CourseOverview overview, Map<String, String> titles) {
            String subject = overview.course().subject().id();
            return new ChapterView(chapter.id(), chapter.title(), chapter.sessions().stream()
                    .map(entry -> SessionEntryView.of(entry, written.contains(entry.id()), overview.finishedSessions().contains(entry.id()),
                            overview.sessionProgress().getOrDefault(entry.id(), new SessionProgress(0, 0, 0)),
                            overview.waitingFor().getOrDefault(entry.id(), List.of()).stream()
                                    .map(id -> PrerequisiteView.of(id, subject, titles))
                                    .toList()))
                    .toList());
        }
    }

    /**
     * {@code questions}, {@code answered}, {@code right}: how far the learner is in the session;
     * {@code waitingFor}: its prerequisites not finished yet.
     */
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
            int right,
            List<PrerequisiteView> waitingFor) {

        static SessionEntryView of(
                SyllabusSession entry, boolean written, boolean finished, SessionProgress progress, List<PrerequisiteView> waitingFor) {
            return new SessionEntryView(entry.id(), entry.title(), entry.kind().name().toLowerCase(Locale.ROOT),
                    entry.duration(), entry.goal(), written, finished, progress.questions(), progress.answered(), progress.right(),
                    waitingFor);
        }
    }

    /** A prerequisite not finished: a session of this subject has its title, one of another its full ID. */
    record PrerequisiteView(String subject, String session, String title) {

        static PrerequisiteView of(SessionId id, String subject, Map<String, String> titles) {
            String title = id.subject().equals(subject) ? titles.getOrDefault(id.session(), id.toString()) : id.toString();
            return new PrerequisiteView(id.subject(), id.session(), title);
        }
    }

    record ScoreView(double earned, double missed, double possible) {
    }

    record TermView(String id, String term, List<String> aliases, String definition, List<String> see) {

        static TermView of(LexiconTerm term) {
            return new TermView(term.id(), term.term(), term.aliases(), term.definition(), term.see());
        }
    }
}
