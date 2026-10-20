package io.github.lucasssoh.boussole.format.validate.rule;

import io.github.lucasssoh.boussole.format.model.Course;
import io.github.lucasssoh.boussole.format.model.LexiconTerm;
import io.github.lucasssoh.boussole.format.model.block.TermInlineDirective;
import java.util.Optional;

/** Finds the term a {@code :term} mention stands for. */
final class LexiconTermResolver {

    private LexiconTermResolver() {
    }

    static Optional<LexiconTerm> resolve(Course course, TermInlineDirective mention) {
        return mention.id().isPresent()
                ? course.lexicon().byId(mention.id().get())
                : course.lexicon().byName(mention.text());
    }
}
