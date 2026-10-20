package io.github.lucasssoh.boussole.format.load;

import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCollector;
import io.github.lucasssoh.boussole.format.model.CourseLexicon;
import io.github.lucasssoh.boussole.format.model.LexiconTerm;
import io.github.lucasssoh.boussole.format.yaml.YamlMap;

/** Reads {@code cursus/lexique.yaml}, which a subject may not have yet. */
final class CourseLexiconReader {

    private CourseLexiconReader() {
    }

    static CourseLexicon read(SubjectFolderFiles files, DiagnosticCollector diagnostics) {
        if (!files.exists(SubjectFolderFiles.LEXICON)) {
            return CourseLexicon.empty();
        }
        return files.read(SubjectFolderFiles.LEXICON)
                .map(text -> YamlMap.parseList(text, YamlMap.YamlSource.of(SubjectFolderFiles.LEXICON, diagnostics)))
                .map(entries -> new CourseLexicon(entries.stream().map(CourseLexiconReader::term).toList()))
                .orElse(CourseLexicon.empty());
    }

    private static LexiconTerm term(YamlMap map) {
        return new LexiconTerm(
                map.requiredText("id"),
                map.requiredText("term"),
                map.texts("aliases"),
                map.requiredText("definition").strip(),
                map.texts("see"),
                map.positionOf("id"));
    }
}
