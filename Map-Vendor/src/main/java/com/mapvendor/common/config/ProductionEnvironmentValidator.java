package com.mapvendor.common.config;

import com.mapvendor.integration.storage.StorageProperties;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class ProductionEnvironmentValidator implements ApplicationRunner {
    private final Environment environment;
    private final StorageProperties storageProperties;

    public ProductionEnvironmentValidator(Environment environment, StorageProperties storageProperties) {
        this.environment = environment;
        this.storageProperties = storageProperties;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!Arrays.asList(environment.getActiveProfiles()).contains("prod")) {
            return;
        }
        require("MAP_VENDOR_DB_PASSWORD");
        require("MAP_VENDOR_FLYWAY_PASSWORD");
        require("MAP_VENDOR_WECHAT_APP_ID");
        require("MAP_VENDOR_WECHAT_APP_SECRET");
        require("MAP_VENDOR_CORS_ALLOWED_ORIGINS");
        String publicBaseUrl = require("MAP_VENDOR_PUBLIC_BASE_URL");
        if (!publicBaseUrl.startsWith("https://")) {
            throw new IllegalStateException("Production public file URL must use HTTPS");
        }
        Path configuredStorageRoot = storageProperties.getRoot();
        Path storageRoot = configuredStorageRoot.toAbsolutePath().normalize();
        Path workingDirectory = Paths.get("").toAbsolutePath().normalize();
        if (!configuredStorageRoot.isAbsolute() || storageRoot.startsWith(workingDirectory)) {
            throw new IllegalStateException("Production storage root must be an absolute directory outside the application");
        }
    }

    private String require(String key) {
        String value = environment.getProperty(key);
        if (!StringUtils.hasText(value)) {
            throw new IllegalStateException("Required production environment variable is missing: " + key);
        }
        return value;
    }
}
