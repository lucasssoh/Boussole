package io.github.lucasssoh.boussole.server.exercise;

import io.github.lucasssoh.boussole.core.learn.AttemptOutcome;
import io.github.lucasssoh.boussole.core.learn.OpenCourse;
import io.github.lucasssoh.boussole.core.learn.RevealNextHint;
import io.github.lucasssoh.boussole.core.learn.RevealSolution;
import io.github.lucasssoh.boussole.core.learn.SubmitAttempt;
import io.github.lucasssoh.boussole.core.learn.progress.ExerciseKey;
import io.github.lucasssoh.boussole.server.block.BlockViewMapper;
import java.util.Optional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/subjects/{subject}/exercises/{exercise}")
class ExerciseController {

    private final SubmitAttempt submitAttempt;
    private final RevealNextHint revealNextHint;
    private final RevealSolution revealSolution;
    private final OpenCourse openCourse;

    ExerciseController(SubmitAttempt submitAttempt, RevealNextHint revealNextHint, RevealSolution revealSolution, OpenCourse openCourse) {
        this.submitAttempt = submitAttempt;
        this.revealNextHint = revealNextHint;
        this.revealSolution = revealSolution;
        this.openCourse = openCourse;
    }

    @PostMapping("/attempts")
    AttemptView attempt(
            @PathVariable String subject,
            @PathVariable String exercise,
            @RequestParam Optional<String> session,
            @RequestBody AttemptRequest request) {
        ExerciseKey key = new ExerciseKey(subject, exercise);
        AttemptOutcome outcome = submitAttempt.submit(key, request.toResponse(), session);
        return AttemptView.of(outcome, SolutionView.of(revealSolution.reveal(key), mapper(subject)));
    }

    @PostMapping("/hints")
    HintView nextHint(@PathVariable String subject, @PathVariable String exercise) {
        return HintView.of(revealNextHint.reveal(new ExerciseKey(subject, exercise)), mapper(subject));
    }

    @GetMapping("/solution")
    SolutionView solution(@PathVariable String subject, @PathVariable String exercise) {
        return SolutionView.of(revealSolution.reveal(new ExerciseKey(subject, exercise)), mapper(subject));
    }

    private BlockViewMapper mapper(String subject) {
        return BlockViewMapper.forContent(openCourse.open(subject).course().subject().sourcesDir());
    }
}
