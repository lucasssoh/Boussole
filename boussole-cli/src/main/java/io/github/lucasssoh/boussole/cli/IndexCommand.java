package io.github.lucasssoh.boussole.cli;

import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCollector;
import io.github.lucasssoh.boussole.format.load.CourseLoader;
import io.github.lucasssoh.boussole.format.model.Course;
import io.github.lucasssoh.boussole.format.model.CourseSession;
import io.github.lucasssoh.boussole.format.model.block.Block;
import io.github.lucasssoh.boussole.format.model.block.BlockWalker;
import io.github.lucasssoh.boussole.format.model.block.ExerciseBlock;
import io.github.lucasssoh.boussole.format.model.block.ExerciseRefBlock;
import io.github.lucasssoh.boussole.format.model.block.McqBlock;
import io.github.lucasssoh.boussole.format.model.block.ProseBlock;
import io.github.lucasssoh.boussole.format.model.part.CheckpointPart;
import io.github.lucasssoh.boussole.format.model.part.NotionPart;
import io.github.lucasssoh.boussole.format.model.part.Part;
import io.github.lucasssoh.boussole.format.model.part.PracticePart;
import io.github.lucasssoh.boussole.format.model.part.TextPart;
import io.github.lucasssoh.boussole.format.model.part.WarmupPart;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

/**
 * {@code boussole index FOLDER}: every question of a track, one line each, with how many times
 * other places ask it again, so that an agent picks the questions of a warmup or a review from
 * this list, the least asked first, without opening every session.
 */
@Command(
        name = "index",
        mixinStandardHelpOptions = true,
        description = {
            "List every question of a subject, one per line: session, part, ID, type, how many times",
            "it is asked again elsewhere (::ref), and the start of its statement.",
            "For warmups and reviews: pick questions asked again rarely, from sessions long ago."
        })
final class IndexCommand implements Callable<Integer> {

    private static final int STATEMENT = 90;

    @Parameters(paramLabel = "FOLDER", description = "The subject folder, the one holding subject.yaml.")
    private Path folder;

    @Option(names = "--before", paramLabel = "SESSION",
            description = "Only the questions of the sessions before this one, in the order of the syllabus.")
    private String before;

    @Spec
    private CommandSpec spec;

    /** A question where it is written: its session, the part it sits in, and its statement. */
    private record Question(String session, String part, String id, String type, String statement) {
    }

    @Override
    public Integer call() {
        PrintWriter out = spec.commandLine().getOut();
        Optional<Course> course = Files.isDirectory(folder) ? CourseLoader.load(folder, new DiagnosticCollector()) : Optional.empty();
        if (course.isEmpty()) {
            spec.commandLine().getErr().println("boussole: " + folder + " has no readable subject.yaml");
            return ExitCode.UNUSABLE;
        }
        List<CourseSession> sessions = sessionsBefore(course.get().sessions());
        List<Question> questions = new ArrayList<>();
        for (CourseSession session : sessions) {
            for (Part part : session.session().parts()) {
                BlockWalker.walk(part.blocks()).forEach(block -> question(session.entry().id(), part, block).ifPresent(questions::add));
            }
        }
        Map<String, Long> askedAgain = course.get().sessions().stream()
                .flatMap(session -> session.session().parts().stream())
                .flatMap(part -> BlockWalker.walk(part.blocks()))
                .filter(ExerciseRefBlock.class::isInstance)
                .collect(Collectors.groupingBy(block -> ((ExerciseRefBlock) block).id(), Collectors.counting()));
        out.println(folder + ": " + questions.size() + " questions in " + sessions.size() + " sessions."
                + " \"again\" is how many times other places ask it again with ::ref.");
        out.printf("%-8s %-22s %-10s %-8s %5s  %s%n", "session", "part", "id", "type", "again", "statement");
        questions.forEach(question -> out.printf("%-8s %-22s %-10s %-8s %5d  %s%n", question.session(), question.part(),
                question.id(), question.type(), askedAgain.getOrDefault(question.id(), 0L), question.statement()));
        return ExitCode.OK;
    }

    private List<CourseSession> sessionsBefore(List<CourseSession> sessions) {
        if (before == null) {
            return sessions;
        }
        List<String> ids = sessions.stream().map(session -> session.entry().id()).toList();
        int end = ids.indexOf(before);
        return end < 0 ? sessions : sessions.subList(0, end);
    }

    private static Optional<Question> question(String session, Part part, Block block) {
        return switch (block) {
            case McqBlock mcq -> Optional.of(new Question(session, partName(part), mcq.id(), mcq.multiple() ? "mcq+" : "mcq",
                    statement(mcq.statement())));
            case ExerciseBlock exercise -> Optional.of(new Question(session, partName(part), exercise.id(),
                    exercise.type().attributeValue(), statement(exercise.statement())));
            default -> Optional.empty();
        };
    }

    private static String partName(Part part) {
        Function<String, String> cut = text -> text.length() <= 22 ? text : text.substring(0, 21) + "…";
        return switch (part) {
            case WarmupPart _ -> "warmup";
            case NotionPart notion -> cut.apply("notion " + notion.id());
            case CheckpointPart _ -> "checkpoint";
            case PracticePart _ -> "practice";
            case TextPart _ -> "introduction";
        };
    }

    /** The start of the statement's text, on one line. */
    private static String statement(List<Block> blocks) {
        String text = blocks.stream()
                .filter(ProseBlock.class::isInstance)
                .map(block -> ((ProseBlock) block).markdown())
                .collect(Collectors.joining(" "))
                .replaceAll("\\s+", " ")
                .strip();
        return text.length() <= STATEMENT ? text : text.substring(0, STATEMENT - 1) + "…";
    }
}
