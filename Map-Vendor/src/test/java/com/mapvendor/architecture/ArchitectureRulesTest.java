package com.mapvendor.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

class ArchitectureRulesTest {
    private final JavaClasses classes = new ClassFileImporter().importPackages("com.mapvendor");

    @Test
    void controllersDoNotAccessRepositoriesOrMappersDirectly() {
        noClasses().that().resideInAPackage("..controller..")
                .should().dependOnClassesThat().resideInAnyPackage("..repository..", "..mapper..")
                .check(classes);
    }

    @Test
    void productionJavaHasNoBackendLoginOrMerchantRoleLegacy() throws Exception {
        Path root = Paths.get("src/main/java");
        StringBuilder source = new StringBuilder();
        try (Stream<Path> paths = Files.walk(root)) {
            paths.filter(path -> path.toString().endsWith(".java")).forEach(path -> {
                try {
                    source.append(new String(Files.readAllBytes(path), StandardCharsets.UTF_8));
                } catch (java.io.IOException exception) {
                    throw new java.io.UncheckedIOException(exception);
                }
            });
        }
        assertThat(source.toString())
                .doesNotContain("BackendPrincipal", "MerchantScope", "/api/v1/merchant/",
                        "PasswordEncoder", "SecurityFilterChain", "Authorization");
    }
}
