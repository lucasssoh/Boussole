package io.github.lucasssoh.boussole.core.learn;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.lucasssoh.boussole.core.learn.progress.SessionKey;
import io.github.lucasssoh.boussole.core.learn.reward.RewardedSubjects;
import io.github.lucasssoh.boussole.format.SubjectInspector;
import io.github.lucasssoh.boussole.format.model.Course;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** A chapter is finished before the chapters that need it: one session of `b` waits for `a`, so all of `b` does. */
class ChapterOrderTest {

    @TempDir
    Path dir;

    private ContentRepository content;
    private final InMemoryProgressRepository progress = new InMemoryProgressRepository();
    private final Clock clock = Clock.fixed(Instant.parse("2026-10-24T10:00:00Z"), ZoneOffset.UTC);

    @BeforeEach
    void subject() throws IOException {
        write("subject.yaml", "format: 1\nid: t\nname: T\nlang: fr\n");
        write("cursus/syllabus.yaml", """
                status: approved
                chapters:
                  - id: a
                    title: A
                    dir: 01-a
                    sessions:
                      - {id: s1, title: Un, duration: 60, goal: Faire., file: s1.md}
                      - {id: s2, title: Deux, duration: 60, goal: Faire., file: s2.md}
                  - id: b
                    title: B
                    dir: 02-b
                    sessions:
                      - {id: s3, title: Trois, duration: 60, goal: Faire., file: s3.md}
                      - {id: s4, title: Quatre, duration: 60, goal: Faire., file: s4.md}
                """);
        session("01-a", "s1", "");
        session("01-a", "s2", "");
        session("02-b", "s3", "");
        session("02-b", "s4", "prerequisites: [t.a.s1]\n");
        Course course = new SubjectInspector().inspect(dir).course().orElseThrow();
        content = () -> List.of(course);
    }

    @Test
    void a_chapter_waits_for_every_chapter_one_of_its_sessions_needs() {
        CourseOverview overview = open();

        assertThat(overview.chapterLinks()).containsEntry("b", Set.of("a")).containsEntry("a", Set.of());
        assertThat(overview.chapterWaitingFor()).isEqualTo(Map.of("b", List.of("a")));
    }

    @Test
    void the_chapter_opens_once_the_one_it_waits_for_is_finished() {
        finish("s1");
        assertThat(open().chapterWaitingFor()).containsKey("b");

        finish("s2");
        assertThat(open().chapterWaitingFor()).isEmpty();
    }

    @Test
    void the_course_resumes_in_a_chapter_that_waits_for_no_other() {
        finish("s1");

        assertThat(open().resume()).contains("s2");
    }

    private CourseOverview open() {
        return new OpenCourse(content, progress).open("t", Optional.empty());
    }

    private void finish(String session) {
        new FinishSession(content, progress, RewardedSubjects.all(), clock).finish(new SessionKey("t", session), Duration.ofMinutes(30));
    }

    private void session(String folder, String id, String frontMatter) throws IOException {
        write("cursus/" + folder + "/" + id + ".md", "---\nid: " + id + "\nobjectives: [Faire.]\n" + frontMatter + "---\n\nUn texte.\n");
    }

    private void write(String name, String text) throws IOException {
        Path path = dir.resolve(name);
        Files.createDirectories(path.getParent());
        Files.writeString(path, text);
    }
}
