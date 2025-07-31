package com.transfernow.TransferNow.config;

import com.transfernow.TransferNow.model.Folder;
import com.transfernow.TransferNow.repository.FileSystemItemRepository;
import com.transfernow.TransferNow.service.FileSystemItemService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MainFolderConfig {

    @Autowired
    private FileSystemItemRepository repository;
    @Autowired
    private FileSystemItemService service;

    @PostConstruct
    public void initMainFolder() {
        if (repository.findById(1L).isEmpty()) {
            Folder main = Folder.builder()
                    .name("main")
                    .build();
            repository.save(main);
        }
    }
}
