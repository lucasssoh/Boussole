package io.github.lucasssoh.boussole.format.validate.rule;

import static io.github.lucasssoh.boussole.format.fixture.Snippets.notion;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.lucasssoh.boussole.format.diagnostic.Diagnostic;
import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCode;
import io.github.lucasssoh.boussole.format.fixture.SubjectFixture;
import io.github.lucasssoh.boussole.format.model.Authorship;
import io.github.lucasssoh.boussole.format.model.AuthorshipRole;
import io.github.lucasssoh.boussole.format.model.SubjectCredit;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AuthorshipRuleTest {

    @TempDir
    Path dir;

    @Test
    void a_session_says_who_wrote_revised_or_translated_it() {
        var report = SubjectFixture.in(dir)
                .subjectYaml("""
                        format: 1
                        id: t
                        name: T
                        lang: fr
                        authors:
                          - name: Université de Nantes
                            organisation: true
                            url: https://www.univ-nantes.fr
                        contributors:
                          - name: Jeanne Martin
                            role: exercices du chapitre 2
                        """)
                .session("s1", """
                        written_by:
                          - model: claude-opus-5-5
                            tool: Claude Code
                            date: 2026-10-21
                          - name: Jeanne Martin
                            role: revised
                        """, notion("a", "e1"))
                .inspect();

        assertThat(report.diagnostics()).isEmpty();
        var course = report.course().orElseThrow();
        assertThat(course.sessions().getFirst().session().header().writtenBy()).containsExactly(
                new Authorship(Optional.of("claude-opus-5-5"), Optional.of("Claude Code"), Optional.empty(),
                        Optional.of(LocalDate.of(2026, 10, 21)), AuthorshipRole.WROTE),
                new Authorship(Optional.empty(), Optional.empty(), Optional.of("Jeanne Martin"), Optional.empty(), AuthorshipRole.REVISED));
        assertThat(course.subject().authors()).containsExactly(
                new SubjectCredit("Université de Nantes", true, Optional.empty(), Optional.of("https://www.univ-nantes.fr")));
        assertThat(course.subject().contributors()).extracting(SubjectCredit::role).containsExactly(Optional.of("exercices du chapitre 2"));
    }

    @Test
    void a_session_without_its_author_is_reported_and_a_line_names_one_author() {
        var report = SubjectFixture.in(dir)
                .session("s1", "written_by: []\n", notion("a", "e1"))
                .session("s2", "written_by:\n  - model: claude-opus-5-5\n    name: Moi\n", notion("b", "e2"))
                .inspect();

        assertThat(report.withCode(DiagnosticCode.P012)).extracting(Diagnostic::message)
                .anySatisfy(message -> assertThat(message).startsWith("session `s1` does not say who wrote it"));
        assertThat(report.withCode(DiagnosticCode.F001)).extracting(Diagnostic::message)
                .containsExactly("each line of `written_by` has a `model` (an AI, by its exact ID) or a `name` (a person), one of them");
    }
}
