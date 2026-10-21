package io.github.lucasssoh.boussole.core.reference;

import java.net.URI;

/** The web, as far as Boussole needs it (port): a look at a page a course cites. */
public interface WebPages {

    /** Never throws: a page that cannot be reached is {@link PagePreview#UNREACHABLE}. */
    PagePreview preview(URI page);
}
