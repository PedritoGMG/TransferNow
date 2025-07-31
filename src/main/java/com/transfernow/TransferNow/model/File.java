package com.transfernow.TransferNow.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

@Data
@Entity
@DiscriminatorValue("FILE")
@NoArgsConstructor
@AllArgsConstructor
public class File extends FileSystemItem {

    private String hardLinkPath;

    @Builder
    public File(Long id, String name, LocalDateTime createdAt, Folder parent, String hardLinkPath) {
        super(id, name, createdAt, parent);
        this.hardLinkPath = hardLinkPath;
    }

    @Override
    public Long getSize() throws IOException {
        return Files.size(Path.of(hardLinkPath));
    }
}
