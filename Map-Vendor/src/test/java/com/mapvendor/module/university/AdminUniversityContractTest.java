package com.mapvendor.module.university;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

class AdminUniversityContractTest {
    @Test
    void staticOpenApiIsValidYamlAndContainsAllUniversityOperations() throws Exception {
        Map<?, ?> document;
        try (InputStream input = Files.newInputStream(Paths.get("docs/openapi/admin-api-contract-v1.yaml"))) {
            document = new Yaml().load(input);
        }
        assertThat(document.get("openapi")).isEqualTo("3.0.3");
        Map<?, ?> paths = (Map<?, ?>) document.get("paths");
        assertThat(paths.keySet()).contains("/api/v1/admin/universities",
                "/api/v1/admin/universities/{id}", "/api/v1/admin/universities/{id}/status");
        assertThat(document.containsKey("security")).isFalse();
    }
}
