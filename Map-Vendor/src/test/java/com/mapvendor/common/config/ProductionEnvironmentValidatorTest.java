package com.mapvendor.common.config;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mapvendor.integration.storage.StorageProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.mock.env.MockEnvironment;

class ProductionEnvironmentValidatorTest {
    @Test
    void ignoresNonProductionProfile() {
        MockEnvironment environment = new MockEnvironment();
        StorageProperties storage = new StorageProperties();
        assertThatCode(() -> new ProductionEnvironmentValidator(environment, storage)
                .run(new DefaultApplicationArguments(new String[0]))).doesNotThrowAnyException();
    }

    @Test
    void rejectsMissingProductionSecrets() {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("prod");
        StorageProperties storage = new StorageProperties();
        assertThatThrownBy(() -> new ProductionEnvironmentValidator(environment, storage)
                .run(new DefaultApplicationArguments(new String[0])))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("MAP_VENDOR_DB_PASSWORD");
    }

    @Test
    void acceptsCompleteProductionEnvironment() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("MAP_VENDOR_DB_PASSWORD", "db-secret")
                .withProperty("MAP_VENDOR_FLYWAY_PASSWORD", "flyway-secret")
                .withProperty("MAP_VENDOR_CORS_ALLOWED_ORIGINS", "https://admin.example.com")
                .withProperty("MAP_VENDOR_PUBLIC_BASE_URL", "https://cdn.example.com/files");
        environment.setActiveProfiles("prod");
        StorageProperties storage = new StorageProperties();
        storage.setRoot("C:/Map-Vendor-Data/images");
        assertThatCode(() -> new ProductionEnvironmentValidator(environment, storage)
                .run(new DefaultApplicationArguments(new String[0]))).doesNotThrowAnyException();
    }
}
