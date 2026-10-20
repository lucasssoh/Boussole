package io.github.lucasssoh.boussole.format.load;

import io.github.lucasssoh.boussole.format.diagnostic.Diagnostics;
import io.github.lucasssoh.boussole.format.model.Lexicon;
import io.github.lucasssoh.boussole.format.model.Term;
import io.github.lucasssoh.boussole.format.yaml.YamlMap;

/** Reads {@code cursus/lexique.yaml}, which a subject may not have yet. */
final class LexiconReader {

    private LexiconReader() {
    }

    static Lexicon read(SubjectFiles files, Diagnostics diagnostics) {
        if (!files.exists(SubjectFiles.LEXICON)) {
            return Lexicon.empty();
        }
        return files.read(SubjectFiles.LEXICON)
                .map(text -> YamlMap.parseList(text, YamlMap.YamlSource.of(SubjectFiles.LEXICON, diagnostics)))
                .map(entries -> new Lexicon(entries.stream().map(LexiconReader::term).toList()))
                .orElse(Lexicon.empty());
    }

    private static Term term(YamlMap map) {
        return new Term(
                map.requiredText("id"),
                map.requiredText("term"),
                map.texts("aliases"),
                map.requiredText("definition").strip(),
                map.texts("see"),
                map.positionOf("id"));
    }
}
