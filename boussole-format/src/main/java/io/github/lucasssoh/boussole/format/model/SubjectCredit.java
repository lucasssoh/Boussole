package io.github.lucasssoh.boussole.format.model;

import java.util.Optional;

/**
 * A name a track credits: one of its authors, the people or organisations that make and sign it
 * (a teacher, a university), or a contributor, with what they did ({@code role}: "relecture",
 * "exercices du chapitre 3"…) and a page about them.
 */
public record SubjectCredit(String name, boolean organisation, Optional<String> role, Optional<String> url) {
}
