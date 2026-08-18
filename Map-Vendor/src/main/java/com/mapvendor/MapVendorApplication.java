package com.mapvendor;

import com.mapvendor.integration.storage.StorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
public class MapVendorApplication {

    public static void main(String[] args) {
        SpringApplication.run(MapVendorApplication.class, args);
    }
}
