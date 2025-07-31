package com.transfernow.TransferNow.repository;

import com.transfernow.TransferNow.model.File;
import com.transfernow.TransferNow.model.FileSystemItem;
import com.transfernow.TransferNow.model.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FileSystemItemRepository extends JpaRepository<FileSystemItem, Long> {
    @Override
    Optional<FileSystemItem> findById(Long aLong);

    @Query("SELECT f FROM FileSystemItem f WHERE f.id = :id AND TYPE(f) = File")
    Optional<File> findFileById(@Param("id") Long id);

    @Query("SELECT f FROM FileSystemItem f WHERE f.id = :id AND TYPE(f) = Folder")
    Optional<Folder> findFolderById(@Param("id") Long id);

    List<FileSystemItem> findByName(String name);
    List<FileSystemItem> findByParent(Folder folder);
}
