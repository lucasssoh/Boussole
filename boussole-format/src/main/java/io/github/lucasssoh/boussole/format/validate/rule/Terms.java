package io.github.lucasssoh.boussole.format.validate.rule;

import io.github.lucasssoh.boussole.format.model.Course;
import io.github.lucasssoh.boussole.format.model.Term;
import io.github.lucasssoh.boussole.format.model.block.TermMention;
import java.util.Optional;

/** Finds the term a {@code :term} mention stands for. */
final class Terms {

    private Terms() {
    }

    static Optional<Term> resolve(Course course, TermMention mention) {
        return mention.id().isPresent()
                ? course.lexicon().byId(mention.id().get())
                : course.lexicon().byName(mention.text());
    }
}
