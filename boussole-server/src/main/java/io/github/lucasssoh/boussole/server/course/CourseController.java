package io.github.lucasssoh.boussole.server.course;

import io.github.lucasssoh.boussole.server.ServerSettings;
import java.util.Set;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Optional;
import io.github.lucasssoh.boussole.core.learn.ListCourses;
import io.github.lucasssoh.boussole.core.learn.OpenCourse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/subjects")
class CourseController {

    private final ListCourses listCourses;
    private final OpenCourse openCourse;
    private final Set<String> builtIn;

    CourseController(ListCourses listCourses, OpenCourse openCourse, ServerSettings settings) {
        this.listCourses = listCourses;
        this.openCourse = openCourse;
        this.builtIn = settings.builtInIds();
    }

    /** {@code lang}: the language each track is shown in when it has it, such as the interface's or the one chosen. */
    @GetMapping
    List<CourseSummaryView> list(@RequestParam Optional<String> lang) {
        return listCourses.list(lang).stream().map(overview -> CourseSummaryView.of(overview, builtIn)).toList();
    }

    @GetMapping("/{subject}")
    CourseView open(@PathVariable String subject, @RequestParam Optional<String> lang) {
        return CourseView.of(openCourse.open(subject, lang));
    }
}
