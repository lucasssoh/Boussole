package io.github.lucasssoh.boussole.server;

import io.github.lucasssoh.boussole.adapters.content.FileContentRepository;
import io.github.lucasssoh.boussole.adapters.web.HttpWebPages;
import io.github.lucasssoh.boussole.core.reference.PreviewWebPage;
import io.github.lucasssoh.boussole.core.reference.WebPages;
import io.github.lucasssoh.boussole.adapters.progress.SqliteDatabase;
import io.github.lucasssoh.boussole.adapters.progress.SqliteProgressRepository;
import io.github.lucasssoh.boussole.core.learn.ContentRepository;
import io.github.lucasssoh.boussole.core.learn.FinishSession;
import io.github.lucasssoh.boussole.core.learn.ListCourses;
import io.github.lucasssoh.boussole.core.learn.OpenCourse;
import io.github.lucasssoh.boussole.core.learn.OpenSession;
import io.github.lucasssoh.boussole.core.learn.RevealNextHint;
import io.github.lucasssoh.boussole.core.learn.RevealSolution;
import io.github.lucasssoh.boussole.core.learn.SubmitAttempt;
import io.github.lucasssoh.boussole.core.learn.progress.ProgressRepository;
import io.github.lucasssoh.boussole.core.learn.reward.ReadStudyStats;
import io.github.lucasssoh.boussole.core.learn.reward.RewardedSubjects;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** The only place where adapters are plugged into the use cases. */
@Configuration(proxyBeanMethods = false)
class LearningConfiguration {

    @Bean
    ContentRepository contentRepository(ServerSettings settings) {
        return new FileContentRepository(settings::subjectsWithBuiltIn);
    }

    @Bean
    ProgressRepository progressRepository(ServerSettings settings) {
        return new SqliteProgressRepository(SqliteDatabase.open(settings.database()));
    }

    /** The tracks built into Boussole are there to be read again: they earn no experience. */
    @Bean
    RewardedSubjects rewardedSubjects(ServerSettings settings) {
        return new RewardedSubjects(settings.builtInIds());
    }

    @Bean
    WebPages webPages() {
        return new HttpWebPages();
    }

    @Bean
    PreviewWebPage previewWebPage(WebPages pages) {
        return new PreviewWebPage(pages);
    }

    @Bean
    Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    ListCourses listCourses(ContentRepository content, ProgressRepository progress) {
        return new ListCourses(content, progress);
    }

    @Bean
    OpenCourse openCourse(ContentRepository content, ProgressRepository progress) {
        return new OpenCourse(content, progress);
    }

    @Bean
    OpenSession openSession(ContentRepository content, ProgressRepository progress) {
        return new OpenSession(content, progress);
    }

    @Bean
    SubmitAttempt submitAttempt(ContentRepository content, ProgressRepository progress, RewardedSubjects rewarded, Clock clock) {
        return new SubmitAttempt(content, progress, rewarded, clock);
    }

    @Bean
    RevealNextHint revealNextHint(ContentRepository content, ProgressRepository progress, Clock clock) {
        return new RevealNextHint(content, progress, clock);
    }

    @Bean
    RevealSolution revealSolution(ContentRepository content, ProgressRepository progress) {
        return new RevealSolution(content, progress);
    }

    @Bean
    ReadStudyStats readStudyStats(ContentRepository content, ProgressRepository progress, RewardedSubjects rewarded, Clock clock) {
        return new ReadStudyStats(content, progress, rewarded, clock);
    }

    @Bean
    FinishSession finishSession(ContentRepository content, ProgressRepository progress, RewardedSubjects rewarded, Clock clock) {
        return new FinishSession(content, progress, rewarded, clock);
    }
}
