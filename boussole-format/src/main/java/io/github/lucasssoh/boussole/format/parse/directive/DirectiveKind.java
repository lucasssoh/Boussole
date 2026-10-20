package io.github.lucasssoh.boussole.format.parse.directive;

/** A container spans several lines ({@code :::name … :::}); a leaf is one line ({@code ::name}). */
public enum DirectiveKind {
    CONTAINER,
    LEAF
}
