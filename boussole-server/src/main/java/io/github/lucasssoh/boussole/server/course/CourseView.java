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
                        .map(chapter -> ChapterView.of(chapter, written, overview, titles, course.syllabus().chapters()))
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

    /**
     * A chapter: {@code after}, the chapters it waits for, those another path gives left out (what a
     * map of the chapters draws); {@code waitingFor}, those of them not finished yet.
     */
    record ChapterView(String id, String title, List<String> after, List<ChapterWaitView> waitingFor, List<SessionEntryView> sessions) {

        static ChapterView of(SyllabusChapter chapter, Set<String> written, CourseOverview overview, Map<String, String> titles,
                List<SyllabusChapter> chapters) {
            String subject = overview.course().subject().id();
            List<String> after = chapters.stream()
                    .map(SyllabusChapter::id)
                    .filter(overview.chapterLinks().getOrDefault(chapter.id(), Set.of())::contains)
                    .toList();
            List<ChapterWaitView> waitingFor = overview.chapterWaitingFor().getOrDefault(chapter.id(), List.of()).stream()
                    .flatMap(id -> chapters.stream().filter(found -> found.id().equals(id)))
                    .map(found -> ChapterWaitView.of(found, written, overview))
                    .toList();
            Map<String, List<String>> before = overview.course().sessions().stream()
                    .filter(session -> session.chapter().id().equals(chapter.id()))
                    .collect(Collectors.toMap(session -> session.entry().id(), session -> session.session().header().prerequisites().stream()
                            .filter(id -> id.startsWith(subject + "." + chapter.id() + "."))
                            .map(id -> id.substring(id.lastIndexOf('.') + 1))
                            .toList()));
            return new ChapterView(chapter.id(), chapter.title(), after, waitingFor, chapter.sessions().stream()
                    .map(entry -> SessionEntryView.of(entry, written.contains(entry.id()), overview.finishedSessions().contains(entry.id()),
                            before.getOrDefault(entry.id(), List.of()),
                            overview.sessionProgress().getOrDefault(entry.id(), new SessionProgress(0, 0, 0)),
                            overview.waitingFor().getOrDefault(entry.id(), List.of()).stream()
                                    .map(id -> PrerequisiteView.of(id, subject, titles))
                                    .toList()))
                    .toList());
        }
    }

    /**
     * {@code after}: the sessions of its chapter it needs, finished or not (what a map of the chapter
     * draws); {@code questions}, {@code answered}, {@code right}: how far the learner is in the
     * session; {@code waitingFor}: its prerequisites not finished yet.
     */
    record SessionEntryView(
            String id,
            String title,
            String kind,
            int duration,
            String goal,
            boolean written,
            boolean finished,
            List<String> after,
            int questions,
            int answered,
            int right,
            List<PrerequisiteView> waitingFor) {

        static SessionEntryView of(SyllabusSession entry, boolean written, boolean finished, List<String> after,
                SessionProgress progress, List<PrerequisiteView> waitingFor) {
            return new SessionEntryView(entry.id(), entry.title(), entry.kind().name().toLowerCase(Locale.ROOT),
                    entry.duration(), entry.goal(), written, finished, after, progress.questions(), progress.answered(), progress.right(),
                    waitingFor);
        }
    }

    /** A chapter to finish first: its title, and the session of it to go on with, the first written not finished. */
    record ChapterWaitView(String id, String title, String next) {

        static ChapterWaitView of(SyllabusChapter chapter, Set<String> written, CourseOverview overview) {
            String next = chapter.sessions().stream()
                    .map(SyllabusSession::id)
                    .filter(id -> written.contains(id) && !overview.finishedSessions().contains(id))
                    .findFirst()
                    .orElse(null);
            return new ChapterWaitView(chapter.id(), chapter.title(), next);
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
