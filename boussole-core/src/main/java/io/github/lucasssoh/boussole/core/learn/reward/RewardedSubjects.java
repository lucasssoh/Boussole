package io.github.lucasssoh.boussole.core.learn.reward;

import java.util.Set;

/**
 * The subjects that earn experience and count in the streak: all of them but those given here,
 * such as the guide built into Boussole, which is there to be read again and is not studying.
 */
public record RewardedSubjects(Set<String> excluded) {

    public RewardedSubjects {
        excluded = Set.copyOf(excluded);
    }

    public static RewardedSubjects all() {
        return new RewardedSubjects(Set.of());
    }

    public static RewardedSubjects allBut(String subject) {
        return new RewardedSubjects(Set.of(subject));
    }

    public boolean rewards(String subject) {
        return !excluded.contains(subject);
    }
}
