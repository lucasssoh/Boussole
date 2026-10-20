package io.github.lucasssoh.boussole.format.model;

/** The full ID of a session: {@code <subject>.<chapter>.<session>}. */
public record SessionId(String subject, String chapter, String session) {

    @Override
    public String toString() {
        return subject + "." + chapter + "." + session;
    }
}
