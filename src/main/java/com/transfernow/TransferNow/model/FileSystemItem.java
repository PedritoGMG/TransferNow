package com.transfernow.TransferNow.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Data
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@NoArgsConstructor
@AllArgsConstructor
public abstract class FileSystemItem {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    @JsonBackReference
    private Folder parent;

    public String getPath() {
        List<String> pathSegments = new ArrayList<>();
        FileSystemItem current = this;

        while (current != null) {
            pathSegments.add(0, current.getName());
            current = current.getParent();
        }

        String path = String.join("/", pathSegments);
        return (this instanceof Folder) ? "/" + path + "/" : "/" + path;
    }

    public Map<Long, String> getPathMap() {
        Map<Long, String> pathMap = new LinkedHashMap<>();
        FileSystemItem current = this;

        while (current != null) {
            pathMap.put(current.getId(), current.getName());
            current = current.getParent();
        }

        List<Map.Entry<Long, String>> entries = new ArrayList<>(pathMap.entrySet());
        Collections.reverse(entries);

        Map<Long, String> invertedMap = new LinkedHashMap<>();
        for (Map.Entry<Long, String> entry : entries) {
            invertedMap.put(entry.getKey(), entry.getValue());
        }

        return invertedMap;
    }

    public abstract Long getSize() throws IOException;
}