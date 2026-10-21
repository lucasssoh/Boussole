package io.github.lucasssoh.boussole.format.model;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Who wrote, revised or translated a session: an AI model (by its exact ID, with the tool that ran
 * it) or a person, and when. The learner sees it, so that how a session was made is never hidden.
 */
public record Authorship(Optional<String> model, Optional<String> tool, Optional<String> name, Optional<LocalDate> date, AuthorshipRole role) {

    /** The model or the person, as written. */
    public String who() {
        return model.or(() -> name).orElse("");
    }
}
