package io.github.lucasssoh.boussole.server.course;

import io.github.lucasssoh.boussole.server.ServerSettings;
import java.util.Set;
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

    @GetMapping
    List<CourseSummaryView> list() {
        return listCourses.list().stream().map(overview -> CourseSummaryView.of(overview, builtIn)).toList();
    }

    @GetMapping("/{subject}")
    CourseView open(@PathVariable String subject) {
        return CourseView.of(openCourse.open(subject));
    }
}
