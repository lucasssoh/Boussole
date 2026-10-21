package io.github.lucasssoh.boussole.format.model;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Where a document of the sources comes from, when it was not handed to the learner: the web page
 * it was taken from or that it sums up, its title, the licence it is shared under, and the day it
 * was retrieved. A track built from nothing cites its sources this way.
 */
public record SourceProvenance(Optional<String> url, Optional<String> title, Optional<String> licence, Optional<LocalDate> retrieved) {

    public static final SourceProvenance NONE = new SourceProvenance(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
}
