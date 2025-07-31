package com.transfernow.TransferNow.service;

import com.transfernow.TransferNow.config.MainFolderConfig;
import com.transfernow.TransferNow.config.StorageConfig;
import com.transfernow.TransferNow.model.File;
import com.transfernow.TransferNow.model.FileSystemItem;
import com.transfernow.TransferNow.model.Folder;
import com.transfernow.TransferNow.repository.FileSystemItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class FileSystemItemService {
    @Autowired
    private FileSystemItemRepository repository;

    @Autowired
    private StorageConfig storageConfig;


    public FileSystemItem create(FileSystemItem item) {
        return repository.save(item);
    }

    public Optional<FileSystemItem> findById(Long id) {
        return repository.findById(id);
    }
    public Optional<Folder> findFolderById(Long id) {
        return repository.findFolderById(id);
    }
    public Optional<File> findFileById(Long id) {
        return repository.findFileById(id);
    }
    public List<FileSystemItem> findByName(String name) {
        return repository.findByName(name);
    }

    public void delete(FileSystemItem item) {
        repository.delete(item);
    }
    public boolean deleteFileById(Long id, boolean fullDelete) {
        Optional<File> file = findFileById(id);
        if (file.isEmpty())
            return false;

        File item = file.get();
        new java.io.File(item.getHardLinkPath()).delete();

        //Delete the Download File | Only if it is in the download dir
        if (fullDelete)
            new java.io.File(storageConfig.getDownloadsPath() + "/" + item.getName()).delete();

        delete(item);
        return true;
    }
    public boolean deleteFolderById(Long id, boolean fullDelete) {
        Optional<Folder> folder = findFolderById(id);
        if (folder.isEmpty())
            return false;

        Folder item = folder.get();
        item.getContents().stream().forEach(
                f -> {
                    if (f instanceof File) {
                        deleteFileById(f.getId(), fullDelete);
                    } else if (f instanceof Folder) {
                        deleteFolderById(f.getId(), fullDelete);
                    }
                }
        );

        delete(item);
        return true;
    }

    public List<FileSystemItem> findByPath(String path) {
        List<FileSystemItem> items = repository.findAll();
        return items.stream().filter(fileSystemItem -> fileSystemItem.equals(path)).collect(Collectors.toList());
    }
    public List<FileSystemItem> findByParent(Folder folder) {
        return repository.findByParent(folder);
    }

    public List<FileSystemItem> findByNameAndPath(String name, String path) {
        List<FileSystemItem> items = repository.findByName(name);
        return items.stream().filter(fileSystemItem -> fileSystemItem.equals(path)).collect(Collectors.toList());
    }

    public String getItemPath(Long id) {
        Optional<FileSystemItem> item = repository.findById(id);
        if (item.isEmpty()) {
            return "";
        }
        return item.get().getPath();
    }

    public Map<Long, String> getItemPathMap(Long id) {
        Optional<FileSystemItem> item = repository.findById(id);
        if (item.isEmpty()) {
            return null;
        }
        return item.get().getPathMap();
    }

    public FileSystemItem save(FileSystemItem item) {
        return repository.save(item);
    }

    public FileSystemItem saveFile(MultipartFile file, Long folderID) {
        Optional<Folder> folder = findFolderById(folderID);
        if (folder.isEmpty())
            return null;
        return saveFile(file, folder.get());
    }
    public FileSystemItem saveFile(MultipartFile file, Folder folder) {

        Path path = Path.of(storageConfig.getDownloadsPath() + "/" + file.getOriginalFilename());

        try {
            file.transferTo(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return saveFileFromPath(path, folder);
    }
    public FileSystemItem saveFileFromPath(Path originalPath) {
        return saveFileFromPath(originalPath, null);
    }
    public FileSystemItem saveFileFromPath(Path originalPath, Folder folder) {
        Path hardLinksDir = storageConfig.getHardLinksPath();

        String hardLinkName = UUID.randomUUID() + "_" + originalPath.getFileName();
        Path hardLinkPath = hardLinksDir.resolve(hardLinkName);

        try {
            Files.createLink(hardLinkPath, originalPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String originalFileName = originalPath.getFileName().toString();
        String uniqueName = getAvailableFileName(originalFileName, folder);

        File file = File.builder()
                .name(uniqueName)
                .hardLinkPath(hardLinkPath.toString())
                .createdAt(LocalDateTime.now())
                .build();
        folder.addItem(file);

        return save(file);
    }

    public List<FileSystemItem> getContentsFromFolder(Folder folder) {
        List<FileSystemItem> items = folder.getContents();
        items.forEach(fileSystemItem -> {
            if (fileSystemItem instanceof Folder)
                ((Folder) fileSystemItem).setContents(new ArrayList<>());
        });
        return items;
    }

    public FileSystemItem createFolder(String name, Folder folder) {
        String uniqueName = getAvailableFolderName(name, folder);

        Folder item = Folder.builder()
                .name(uniqueName)
                .createdAt(LocalDateTime.now())
                .build();
        folder.addItem(item);
        return save(item);
    }

    private boolean nameExists(Folder folder, String name) {
        return folder.getContents().stream()
                .anyMatch(f -> f.getName().equals(name));
    }
    private String getAvailableFileName(String fileName, Folder folder) {
        String nameWithoutExt;
        String extension;

        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex != -1) {
            nameWithoutExt = fileName.substring(0, dotIndex);
            extension = fileName.substring(dotIndex);
        } else {
            nameWithoutExt = fileName;
            extension = "";
        }

        String candidateName = fileName;
        int counter = 2;

        while (nameExists(folder, candidateName)) {
            candidateName = nameWithoutExt + counter + extension;
            counter++;
        }

        return candidateName;
    }
    private String getAvailableFolderName(String name, Folder folder) {
        String candidateName = name;
        int counter = 2;

        while (nameExists(folder, candidateName)) {
            candidateName = name + counter;
            counter++;
        }

        return candidateName;
    }

    @Transactional
    public Resource downloadItem(Long id) throws IOException {
        FileSystemItem item = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        if (item instanceof File) {
            return downloadFile((File) item);
        } else if (item instanceof Folder) {
            return downloadFolder((Folder) item);
        }

        throw new RuntimeException("Unsupported item type");
    }
    private Resource downloadFile(File file) throws IOException {
        Path filePath = Path.of(file.getHardLinkPath());
        if (!Files.exists(filePath)) {
            throw new RuntimeException("Physical file not found");
        }

        return new PathResource(filePath);
    }
    private Resource downloadFolder(Folder folder) throws IOException {
        Path tempZip = Files.createTempFile(folder.getName(), ".zip");

        try (ZipOutputStream zipOut = new ZipOutputStream(Files.newOutputStream(tempZip))) {
            zipFolder(folder, zipOut, "");
        }

        return new PathResource(tempZip) {
            public void close() throws IOException {
                Files.deleteIfExists(tempZip);
            }
        };
    }
    private void zipFolder(Folder folder, ZipOutputStream zipOut, String parentPath) throws IOException {
        for (FileSystemItem item : folder.getContents()) {
            String itemPath = parentPath + item.getName();

            if (item instanceof Folder) {
                zipOut.putNextEntry(new ZipEntry(itemPath + "/"));
                zipOut.closeEntry();
                zipFolder((Folder) item, zipOut, itemPath + "/");
            } else if (item instanceof File) {
                Path filePath = Path.of(((File) item).getHardLinkPath());
                if (Files.exists(filePath)) {
                    zipOut.putNextEntry(new ZipEntry(itemPath));
                    Files.copy(filePath, zipOut);
                    zipOut.closeEntry();
                }
            }
        }
    }
}
