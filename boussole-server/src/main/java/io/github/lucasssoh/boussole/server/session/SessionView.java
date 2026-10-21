package io.github.lucasssoh.boussole.server.session;

import io.github.lucasssoh.boussole.server.block.BlockView;
import io.github.lucasssoh.boussole.server.block.BlockViewMapper;
import io.github.lucasssoh.boussole.server.block.SourcePaths;
import io.github.lucasssoh.boussole.core.learn.OpenedSession;
import io.github.lucasssoh.boussole.format.model.Course;
import io.github.lucasssoh.boussole.format.model.CourseSession;
import io.github.lucasssoh.boussole.format.model.SessionFrontMatter;
import io.github.lucasssoh.boussole.format.model.SyllabusSession;
import io.github.lucasssoh.boussole.format.model.block.ExerciseBlock;
import io.github.lucasssoh.boussole.format.model.block.ExerciseRefBlock;
import io.github.lucasssoh.boussole.format.model.block.McqBlock;
import io.github.lucasssoh.boussole.format.model.block.QuestionTimeEstimator;
import io.github.lucasssoh.boussole.format.model.part.CheckpointPart;
import io.github.lucasssoh.boussole.format.model.part.NotionPart;
import io.github.lucasssoh.boussole.format.model.part.Part;
import io.github.lucasssoh.boussole.format.model.part.PracticePart;
import io.github.lucasssoh.boussole.format.model.part.TextPart;
import io.github.lucasssoh.boussole.format.model.part.WarmupPart;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

/**
 * A session as the player shows it, in {@code lang}, one of the {@code languages} of its track. {@code file} is the session file in the subject folder:
 * relative links and images of the Markdown resolve against it. {@code questionMinutes} is the
 * time its questions take at a normal pace, which a timed session fits to its {@code duration}.
 */
record SessionView(
        String subject,
        String lang,
        List<String> languages,
        String id,
        String title,
        String kind,
        int duration,
        String goal,
        String chapterId,
        String chapterTitle,
        String file,
        List<String> objectives,
        List<String> prerequisites,
        List<SourceView> sources,
        List<PartView> parts,
        double questionMinutes,
        String previous,
        String next) {

    static SessionView of(OpenedSession opened) {
        Course course = opened.course();
        CourseSession session = opened.session();
        SyllabusSession entry = session.entry();
        SessionFrontMatter header = session.session().header();
        String sourcesDir = course.subject().sourcesDir();
        BlockViewMapper mapper = new BlockViewMapper(sourcesDir, opened.referenced(), opened.progress(), opened.answered());
        List<String> written = course.sessions().stream().map(found -> found.entry().id()).toList();
        int index = written.indexOf(entry.id());
        return new SessionView(
                course.subject().id(),
                course.subject().lang(),
                opened.languages(),
                entry.id(),
                entry.title(),
                entry.kind().name().toLowerCase(Locale.ROOT),
                entry.duration(),
                entry.goal(),
                session.chapter().id(),
                session.chapter().title(),
                session.file(),
                header.objectives(),
                header.prerequisites(),
                header.sources().stream()
                        .map(source -> new SourceView(SourcePaths.of(sourcesDir, source.file()),
                                source.page().isPresent() ? source.page().getAsInt() : null))
                        .toList(),
                session.session().parts().stream().map(part -> PartView.of(part, mapper)).toList(),
                questionMinutes(opened),
                index > 0 ? written.get(index - 1) : null,
                index >= 0 && index + 1 < written.size() ? written.get(index + 1) : null);
    }

    private static double questionMinutes(OpenedSession opened) {
        return opened.session().session().blocks()
                .flatMap(block -> block instanceof ExerciseRefBlock ref
                        ? Stream.ofNullable(opened.referenced().get(ref.id()))
                        : Stream.of(block))
                .mapToDouble(block -> switch (block) {
                    case McqBlock mcq -> QuestionTimeEstimator.minutes(mcq);
                    case ExerciseBlock exercise -> QuestionTimeEstimator.minutes(exercise);
                    default -> 0;
                })
                .sum();
    }

    record SourceView(String path, Integer page) {
    }

    /** {@code kind} is {@code text}, {@code warmup}, {@code notion}, {@code checkpoint} or {@code practice}. */
    record PartView(String kind, String id, String title, List<BlockView> blocks) {

        static PartView of(Part part, BlockViewMapper mapper) {
            List<BlockView> blocks = mapper.blocks(part.blocks());
            return switch (part) {
                case TextPart _ -> new PartView("text", null, null, blocks);
                case WarmupPart _ -> new PartView("warmup", null, null, blocks);
                case NotionPart notion -> new PartView("notion", notion.id(), notion.title(), blocks);
                case CheckpointPart _ -> new PartView("checkpoint", null, null, blocks);
                case PracticePart _ -> new PartView("practice", null, null, blocks);
            };
        }
    }
}
