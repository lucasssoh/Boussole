package io.github.lucasssoh.boussole.format.fixture;

import io.github.lucasssoh.boussole.format.InspectionReport;
import io.github.lucasssoh.boussole.format.SubjectInspector;
import io.github.lucasssoh.boussole.format.validate.Validator;
import io.github.lucasssoh.boussole.format.validate.VideoCatalog;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

/**
 * Builds a subject folder for a test: one chapter {@code ch} in {@code cursus/01-ch}, whose
 * sessions are written as given. Everything not given has a valid default.
 */
public final class SubjectFixture {

    public static final String CHAPTER = "ch";
    private static final String DIR = "01-ch";

    private final Path root;
    private final Map<String, Optional<String>> sessions = new LinkedHashMap<>();
    private final Map<String, String> timing = new LinkedHashMap<>();
    private String status = "approved";
    private Optional<String> subjectYaml = Optional.empty();
    private Optional<String> syllabusYaml = Optional.empty();
    private Optional<String> lexiconYaml = Optional.empty();
    private Optional<String> learnerYaml = Optional.empty();
    private Optional<String> sourcesYaml = Optional.empty();
    private Optional<VideoCatalog> videos = Optional.empty();

    private SubjectFixture(Path root) {
        this.root = root;
    }

    public static SubjectFixture in(Path root) {
        return new SubjectFixture(root);
    }

    /** A session with a minimal front matter ({@code id} and one objective) and this body. */
    public SubjectFixture session(String id, String body) {
        return session(id, "", body);
    }

    /** A session whose front matter has these extra YAML lines. */
    public SubjectFixture session(String id, String frontMatter, String body) {
        return sessionFile(id, "---\nid: " + id + "\nobjectives: [Faire quelque chose.]\n" + frontMatter + "---\n" + body);
    }

    /** A session file written exactly as given. */
    public SubjectFixture sessionFile(String id, String content) {
        sessions.put(id, Optional.of(content));
        return this;
    }

    /** A review or an exam allowing {@code minutes}, with a minimal front matter and this body. */
    public SubjectFixture timedSession(String id, String kind, int minutes, String body) {
        timing.put(id, "\n        kind: " + kind + "\n        duration: " + minutes);
        return session(id, body);
    }

    /** A session planned in the syllabus, without a file. */
    public SubjectFixture plannedSession(String id) {
        sessions.put(id, Optional.empty());
        return this;
    }

    public SubjectFixture status(String status) {
        this.status = status;
        return this;
    }

    public SubjectFixture subjectYaml(String yaml) {
        this.subjectYaml = Optional.of(yaml);
        return this;
    }

    public SubjectFixture syllabusYaml(String yaml) {
        this.syllabusYaml = Optional.of(yaml);
        return this;
    }

    public SubjectFixture lexicon(String yaml) {
        this.lexiconYaml = Optional.of(yaml);
        return this;
    }

    public SubjectFixture learner(String yaml) {
        this.learnerYaml = Optional.of(yaml);
        return this;
    }

    public SubjectFixture sources(String yaml) {
        this.sourcesYaml = Optional.of(yaml);
        return this;
    }

    /** A document of the sources folder that is not a PDF, with the given content. */
    public SubjectFixture document(String name, String content) {
        write("sources/" + name, content);
        return this;
    }

    public SubjectFixture pdf(String name, int pages) {
        try (PDDocument document = new PDDocument()) {
            for (int page = 0; page < pages; page++) {
                document.addPage(new PDPage());
            }
            Files.createDirectories(root.resolve("sources"));
            document.save(root.resolve("sources").resolve(name).toFile());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return this;
    }

    public SubjectFixture videos(VideoCatalog catalog) {
        this.videos = Optional.of(catalog);
        return this;
    }

    public InspectionReport inspect() {
        write("subject.yaml", subjectYaml.orElse("format: 1\nid: test\nname: Test\nlang: fr\n"));
        write("cursus/syllabus.yaml", syllabusYaml.orElseGet(this::syllabus));
        lexiconYaml.ifPresent(yaml -> write("cursus/lexique.yaml", yaml));
        learnerYaml.ifPresent(yaml -> write("learner.yaml", yaml));
        sourcesYaml.ifPresent(yaml -> write("cursus/sources.yaml", yaml));
        sessions.forEach((id, content) -> content.ifPresent(text -> write("cursus/" + DIR + "/" + id + ".md", text)));
        return new SubjectInspector(Validator.standard(), videos).inspect(root);
    }

    private String syllabus() {
        StringBuilder yaml = new StringBuilder("status: " + status + "\nchapters:\n  - id: " + CHAPTER
                + "\n    title: Chapitre\n    dir: " + DIR + "\n    sessions:\n");
        sessions.keySet().forEach(id -> yaml.append("      - id: ").append(id)
                .append("\n        title: Séance ").append(id)
                .append(timing.getOrDefault(id, "\n        duration: 60"))
                .append("\n        goal: Faire quelque chose.\n        file: ").append(id).append(".md\n"));
        if (sessions.isEmpty()) {
            yaml.append("      []\n");
        }
        return yaml.toString();
    }

    private void write(String name, String content) {
        try {
            Path path = root.resolve(name);
            Files.createDirectories(path.getParent());
            Files.writeString(path, content);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
