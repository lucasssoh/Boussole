package io.github.lucasssoh.boussole.format.validate.rule;

import static io.github.lucasssoh.boussole.format.fixture.Snippets.exercise;
import static io.github.lucasssoh.boussole.format.fixture.Snippets.notion;
import static io.github.lucasssoh.boussole.format.fixture.Snippets.mcq;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCode;
import io.github.lucasssoh.boussole.format.fixture.SubjectFixture;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ReferenceRuleTest {

    @TempDir
    Path dir;

    private static final String LEXICON = """
            - id: mac
              term: Adresse MAC
              aliases: [MAC]
              definition: Une adresse.
              see: [arp]
            """;

    @Test
    void aRefPointsToAnExerciseOfTheSubject() {
        var report = SubjectFixture.in(dir)
                .session("s1", notion("a", "e1"))
                .session("s2", ":::::warmup\n::ref{id=e1}\n::ref{id=e9}\n:::::\n" + notion("b", "e2"))
                .inspect();

        assertThat(report.withCode(DiagnosticCode.F030)).singleElement()
                .satisfies(diagnostic -> assertThat(diagnostic.message()).contains("`e9`"));
    }

    @Test
    void aTermIsFoundByIdNameOrAlias() {
        var report = SubjectFixture.in(dir)
                .lexicon(LEXICON.replace("see: [arp]", "see: []"))
                .session("s1", "terms: [mac]\n",
                        "Les :term[adresses]{id=mac}, une :term[adresse mac] et la :term[MAC].\n\n" + notion("a", "e1"))
                .inspect();

        assertThat(report.withCode(DiagnosticCode.F030)).isEmpty();
    }

    @Test
    void anUnknownTermIsReported() {
        var report = SubjectFixture.in(dir)
                .lexicon(LEXICON.replace("see: [arp]", "see: []"))
                .session("s1", "Une :term[trame] et une :term[adresse]{id=ip}.\n\n" + notion("a", "e1"))
                .inspect();

        assertThat(report.withCode(DiagnosticCode.F030)).extracting(diagnostic -> diagnostic.message())
                .containsExactly(
                        "`:term[trame]` matches no term or alias of the lexicon: add {id=…}",
                        "`:term` points to `ip`, which is not in the lexicon");
    }

    @Test
    void frontMatterAndLexiconReferencesExist() {
        var report = SubjectFixture.in(dir)
                .lexicon(LEXICON)
                .session("s1", "terms: [ip]\nprerequisites: [test.ch.s1, test.ch.s9]\n", notion("a", "e1"))
                .inspect();

        assertThat(report.withCode(DiagnosticCode.F030)).extracting(diagnostic -> diagnostic.message())
                .containsExactlyInAnyOrder(
                        "term `mac` sees `arp`, which is not in the lexicon",
                        "`terms` lists `ip`, which is not in the lexicon",
                        "prerequisite `test.ch.s9` is not a session of the syllabus (write <subject>.<chapter>.<session>)");
    }

    @Test
    void aRefAsksOnlyAboutAnEarlierSession() {
        var report = SubjectFixture.in(dir)
                .session("s1", notion("a", "e1"))
                .session("s2", ":::::warmup\n::ref{id=e1}\n::ref{id=e3}\n:::::\n" + notion("b", "e2"))
                .session("s3", ":::::warmup\n::ref{id=e3}\n:::::\n" + notion("c", "e3"))
                .inspect();

        assertThat(report.withCode(DiagnosticCode.F042)).extracting(diagnostic -> diagnostic.message())
                .containsExactlyInAnyOrder(
                        "`::ref{id=e3}` asks a question of `s3`, which comes after `s2`: ask again only questions of earlier sessions, or write a new question here",
                        "`::ref{id=e3}` asks a question of `s3`, which is this session: ask again only questions of earlier sessions, or write a new question here");
    }
}
