package io.github.lucasssoh.boussole.core.learn;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.lucasssoh.boussole.core.learn.check.ChoicesResponse;
import io.github.lucasssoh.boussole.core.learn.check.Verdict;
import io.github.lucasssoh.boussole.core.learn.progress.FirstAnswer;
import io.github.lucasssoh.boussole.core.learn.progress.ExerciseKey;
import io.github.lucasssoh.boussole.core.learn.reward.RewardedSubjects;
import io.github.lucasssoh.boussole.format.SubjectInspector;
import io.github.lucasssoh.boussole.format.model.Course;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** A track in several languages, on the guide shipped in English and French. */
class TrackLanguagesTest {

    private static final Path GUIDE = Path.of("../guide/getting-started");

    private final ContentRepository content = new ContentRepository() {
        private final List<Course> versions = List.of(read("en"), read("fr"));

        @Override
        public List<Course> courses() {
            return versions;
        }
    };
    private final InMemoryProgressRepository progress = new InMemoryProgressRepository();
    private final Clock clock = Clock.fixed(Instant.parse("2026-10-21T10:00:00Z"), ZoneOffset.UTC);

    private static Course read(String language) {
        return new SubjectInspector().inspect(GUIDE.resolve(language)).course().orElseThrow();
    }

    @Test
    void a_track_is_listed_once_in_the_language_asked_for_with_all_its_languages() {
        List<CourseOverview> tracks = new ListCourses(content, progress).list(Optional.of("fr"));

        assertThat(tracks).singleElement().satisfies(track -> {
            assertThat(track.course().subject().lang()).isEqualTo("fr");
            assertThat(track.languages()).containsExactly("en", "fr");
        });
        assertThat(new ListCourses(content, progress).list(Optional.of("es")).getFirst().course().subject().lang()).isEqualTo("en");
    }

    @Test
    void progress_is_the_same_in_every_language() {
        new SubmitAttempt(content, progress, RewardedSubjects.all(), clock)
                .submit(new ExerciseKey("boussole-guide", "g1-a1"), new ChoicesResponse(Set.of(1)), Optional.of("g1"), Optional.of("en"));

        OpenedSession french = new OpenSession(content, progress).open("boussole-guide", "g1", Optional.of("fr"));

        assertThat(french.course().subject().lang()).isEqualTo("fr");
        assertThat(french.answered()).containsEntry("g1-a1", new FirstAnswer(Verdict.CORRECT, 3));
        assertThatThrownBy(() -> new SubmitAttempt(content, progress, RewardedSubjects.all(), clock)
                .submit(new ExerciseKey("boussole-guide", "g1-a1"), new ChoicesResponse(Set.of(1)), Optional.of("g1"), Optional.of("fr")))
                .isInstanceOf(LearningRuleException.class);
    }
}
