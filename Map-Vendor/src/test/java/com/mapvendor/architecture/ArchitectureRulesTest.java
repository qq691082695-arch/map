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

    @Test
    void publishedContractsHaveNoMerchantRoutesOrBackendAuthenticationSchemes() throws Exception {
        String contracts = readTree(Paths.get("docs/openapi"));
        assertThat(contracts.toLowerCase())
                .doesNotContain("\n  /api/v1/merchant", "\n  /merchant", "bearerauth", "basicAuth".toLowerCase(),
                        "securityschemes:");
    }

    @Test
    void dishRecommendationMigrationSupportsPreExistingLocalSchema() throws Exception {
        String sql = new String(Files.readAllBytes(Paths.get(
                "src/main/resources/db/migration/V003__dish_recommendation.sql")), StandardCharsets.UTF_8);
        assertThat(sql).contains("information_schema.columns", "information_schema.statistics",
                "PREPARE add_recommended_column_stmt", "PREPARE add_recommended_index_stmt");
    }

    private String readTree(Path root) throws Exception {
        StringBuilder content = new StringBuilder();
        try (Stream<Path> paths = Files.walk(root)) {
            paths.filter(Files::isRegularFile).forEach(path -> {
                try {
                    content.append(new String(Files.readAllBytes(path), StandardCharsets.UTF_8));
                } catch (java.io.IOException exception) {
                    throw new java.io.UncheckedIOException(exception);
                }
            });
        }
        return content.toString();
    }
}
