package com.transfernow.TransferNow.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Configuration
public class StorageConfig {

    @Value("${app.storage.hardlinks.dir}")
    private String hardLinksDir;

    @Value("${app.storage.downloads.dir}")
    private String downloadsDir;

    public Path getHardLinksPath() {
        return Path.of(hardLinksDir);
    }

    public Path getDownloadsPath() {
        return Path.of(downloadsDir);
    }

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(getHardLinksPath());
        Files.createDirectories(getDownloadsPath());
    }
}
