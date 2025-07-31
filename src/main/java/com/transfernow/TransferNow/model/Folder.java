package com.transfernow.TransferNow.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@DiscriminatorValue("FOLDER")

@NoArgsConstructor
@AllArgsConstructor
public class Folder extends FileSystemItem {

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<FileSystemItem> contents = new ArrayList<>();

    @Builder
    public Folder(Long id, String name, LocalDateTime createdAt, Folder parent, List<FileSystemItem> contents) {
        super(id, name, createdAt, parent);
        this.contents = contents;
    }

    public void addItem(FileSystemItem item) {
        item.setParent(this);
        this.contents.add(item);
    }

    @SneakyThrows
    @Override
    public Long getSize() throws IOException {
        return contents.stream()
                .mapToLong(item -> {
                    try {
                        return item.getSize();
                    } catch (IOException e) {
                        return 0L;
                    }
                }).sum();
    }
}
