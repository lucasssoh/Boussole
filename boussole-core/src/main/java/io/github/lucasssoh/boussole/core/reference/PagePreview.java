package io.github.lucasssoh.boussole.core.reference;

import java.util.Optional;

/**
 * What Boussole learns of a web page before showing it beside the text: whether it answers,
 * whether it lets another page show it in a frame, and its title.
 */
public record PagePreview(boolean reachable, boolean embeddable, Optional<String> title) {

    public static final PagePreview UNREACHABLE = new PagePreview(false, false, Optional.empty());
}
