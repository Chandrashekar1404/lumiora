package com.lumiora.document.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

@Configuration
public class DocumentStorageConfig {

    @Bean
    public Path documentStoragePath(
            @Value("${lumiora.storage.location:./uploads/documents}")
            String storageLocation) {

        return Path.of(storageLocation)
                .toAbsolutePath()
                .normalize();
    }
}