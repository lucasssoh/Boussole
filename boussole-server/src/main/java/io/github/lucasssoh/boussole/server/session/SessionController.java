package io.github.lucasssoh.boussole.server.session;

import io.github.lucasssoh.boussole.core.learn.FinishSession;
import io.github.lucasssoh.boussole.core.learn.OpenSession;
import io.github.lucasssoh.boussole.core.learn.progress.SessionKey;
import java.time.Duration;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/subjects/{subject}/sessions/{session}")
class SessionController {

    private final OpenSession openSession;
    private final FinishSession finishSession;

    SessionController(OpenSession openSession, FinishSession finishSession) {
        this.openSession = openSession;
        this.finishSession = finishSession;
    }

    @GetMapping
    SessionView open(@PathVariable String subject, @PathVariable String session) {
        return SessionView.of(openSession.open(subject, session));
    }

    @PostMapping("/finish")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void finish(@PathVariable String subject, @PathVariable String session, @RequestBody FinishRequest request) {
        finishSession.finish(new SessionKey(subject, session), Duration.ofSeconds(request.spentSeconds()));
    }

    /** The time actually spent on the session, measured by the interface. */
    record FinishRequest(long spentSeconds) {
    }
}
