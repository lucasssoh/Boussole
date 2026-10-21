package io.github.lucasssoh.boussole.core.learn;

import io.github.lucasssoh.boussole.format.SubjectInspector;
import io.github.lucasssoh.boussole.format.model.Course;
import java.nio.file.Path;
import java.util.List;

/** The example subject of the repository, {@code reseaux}. */
final class ExampleContentRepository implements ContentRepository {

    private final Course reseaux = new SubjectInspector().inspect(Path.of("../examples/reseaux")).course().orElseThrow();

    @Override
    public List<Course> courses() {
        return List.of(reseaux);
    }
}
