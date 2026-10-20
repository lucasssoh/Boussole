package io.github.lucasssoh.boussole.format.fixture;

/** Pieces of session bodies that are valid on their own, to build tests from. */
public final class Snippets {

    private Snippets() {
    }

    /** A text exercise with a hint and a solution. */
    public static String exercise(String id) {
        return """
                ::::exercise{#%s type=text}
                Question ?

                :::answer
                oui
                :::

                :::hint
                Indice.
                :::

                :::solution
                Oui.
                :::
                ::::
                """.formatted(id);
    }

    /** A notion with one example and one exercise. */
    public static String notion(String id, String exerciseId) {
        return """
                ## Notion %s {#%s}

                Explication.

                :::example[Exemple]
                Exemple corrigé.
                :::

                """.formatted(id, id) + exercise(exerciseId);
    }

    /** A qcm with these choice lines and a solution. */
    public static String qcm(String attributes, String choices) {
        return """
                ::::qcm{%s}
                Question ?

                %s

                :::solution
                Explication.
                :::
                ::::
                """.formatted(attributes, choices);
    }
}
