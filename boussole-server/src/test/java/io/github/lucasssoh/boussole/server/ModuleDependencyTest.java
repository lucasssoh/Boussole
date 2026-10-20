package io.github.lucasssoh.boussole.server;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

/** Dependencies point down only: format, then core, then adapters, then the server. */
class ModuleDependencyTest {

    private static final String ROOT = "io.github.lucasssoh.boussole";

    private final JavaClasses classes = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages(ROOT);

    @Test
    void the_format_knows_nothing_above_it() {
        noClasses().that().resideInAPackage(ROOT + ".format..")
                .should().dependOnClassesThat().resideInAnyPackage(ROOT + ".core..", ROOT + ".adapters..", ROOT + ".server..")
                .check(classes);
    }

    @Test
    void the_core_uses_no_framework_nor_the_outside_world() {
        noClasses().that().resideInAPackage(ROOT + ".core..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        ROOT + ".adapters..", ROOT + ".server..", "org.springframework..", "java.sql..", "java.net.http..")
                .check(classes);
    }

    @Test
    void the_adapters_do_not_know_the_server() {
        noClasses().that().resideInAPackage(ROOT + ".adapters..")
                .should().dependOnClassesThat().resideInAnyPackage(ROOT + ".server..", "org.springframework..")
                .check(classes);
    }
}
