package io.github.lucasssoh.boussole.server.course;

import io.github.lucasssoh.boussole.format.model.Authorship;
import io.github.lucasssoh.boussole.format.model.Course;
import io.github.lucasssoh.boussole.format.model.SubjectCredit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Who makes a track and its sessions, as the interface shows it. */
public final class CreditViews {

    private CreditViews() {
    }

    /** An author or a contributor of a track; {@code role} and {@code url} are null when not given. */
    public record CreditView(String name, boolean organisation, String role, String url) {

        static CreditView of(SubjectCredit credit) {
            return new CreditView(credit.name(), credit.organisation(), credit.role().orElse(null), credit.url().orElse(null));
        }
    }

    /**
     * Who wrote, revised or translated a session: {@code model} and {@code tool} for an AI,
     * {@code name} for a person (the others null), {@code date} as 2026-10-21 or null.
     */
    public record AuthorshipView(String model, String tool, String name, String date, String role) {

        public static AuthorshipView of(Authorship authorship) {
            return new AuthorshipView(authorship.model().orElse(null), authorship.tool().orElse(null),
                    authorship.name().orElse(null), authorship.date().map(Object::toString).orElse(null),
                    authorship.role().attributeValue());
        }
    }

    /** A model or a person and the number of sessions of the track they wrote. */
    public record WriterView(String who, boolean model, int sessions) {
    }

    static List<CreditView> credits(List<SubjectCredit> credits) {
        return credits.stream().map(CreditView::of).toList();
    }

    /** The first writer of each session, counted: who wrote the track, in short. */
    static List<WriterView> writers(Course course) {
        Map<String, WriterView> writers = new LinkedHashMap<>();
        course.sessions().stream()
                .flatMap(session -> session.session().header().writtenBy().stream().findFirst().stream())
                .forEach(first -> writers.merge(first.who(), new WriterView(first.who(), first.model().isPresent(), 1),
                        (known, more) -> new WriterView(known.who(), known.model(), known.sessions() + 1)));
        return List.copyOf(writers.values());
    }
}
