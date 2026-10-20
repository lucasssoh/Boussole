package io.github.lucasssoh.boussole.format.parse.directive;

import java.util.HashSet;
import java.util.Set;

/** Names of the places a directive can appear in. */
public final class Parents {

    /** The body of a session: a notion or a text part. */
    public static final String TOP = "";

    public static final Set<String> SECTIONS = Set.of("warmup", "checkpoint", "practice");
    public static final Set<String> EXERCISE_PARTS = Set.of("answer", "hint", "solution");

    /** Where text and exercises flow: the body and the sections. */
    public static final Set<String> FLOW = union(Set.of(TOP), SECTIONS);

    static final Set<String> MEDIA = union(FLOW, Set.of("example", "recall", "note", "tip", "warning", "exercise", "qcm", "hint", "solution"));

    private Parents() {
    }

    public static String describe(String parent) {
        return parent.equals(TOP) ? "the body of a session" : "`" + parent + "`";
    }

    @SafeVarargs
    static Set<String> union(Set<String>... sets) {
        Set<String> all = new HashSet<>();
        for (Set<String> set : sets) {
            all.addAll(set);
        }
        return Set.copyOf(all);
    }
}
