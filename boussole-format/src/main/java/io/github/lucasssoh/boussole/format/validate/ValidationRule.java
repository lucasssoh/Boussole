package io.github.lucasssoh.boussole.format.validate;

import io.github.lucasssoh.boussole.format.diagnostic.Diagnostics;

/** One rule of {@code FORMAT.md} §6 that needs the whole course to be checked. */
public interface ValidationRule {

    void check(ValidationContext context, Diagnostics diagnostics);
}
