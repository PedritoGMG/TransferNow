package com.transfernow.TransferNow.controller;

import com.transfernow.TransferNow.model.File;
import com.transfernow.TransferNow.model.FileSystemItem;
import com.transfernow.TransferNow.model.Folder;
import com.transfernow.TransferNow.service.FileSystemItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/filesystem")
public class FileSystemItemController {
    @Autowired
    private FileSystemItemService service;

    @GetMapping("/{id}/path")
    public ResponseEntity<String> getPath(@PathVariable Long id) {
        String item = service.getItemPath(id);
        if (item.isEmpty()) {
            return ResponseEntity.badRequest().body("File not Found");
        }
        return ResponseEntity.ok(service.getItemPath(id));
    }

    @GetMapping("/pathMap/{id}")
    public ResponseEntity<Map<Long, String>> getPathMap(@PathVariable Long id) {
        Map<Long, String> item = service.getItemPathMap(id);
        if (item == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(item);
    }

    @PostMapping("/addFile")
    public ResponseEntity<String> addFile(@RequestParam("file") MultipartFile file, @RequestParam Long idFolder) throws IOException {
        Optional<Folder> folder = service.findFolderById(idFolder);
        if (folder.isEmpty())
            return ResponseEntity.badRequest().body("There is no folder with the id set");

        FileSystemItem item = service.saveFile(file, folder.get());
        return ResponseEntity.ok("File Received: " + item.getName() + " (" + item.getSize() + " bytes)");
    }

    @PostMapping("/addFileFromPath")
    public ResponseEntity<String> addFileFromPath(@RequestBody String path, @RequestParam Long idFolder) throws IOException {

        Optional<Folder> folder = service.findFolderById(idFolder);
        if (folder.isEmpty())
            return ResponseEntity.badRequest().body("There is no folder with the id set");

        Path originalPath = Path.of(path);
        FileSystem file = originalPath.getFileSystem();

        if (file == null)
            return ResponseEntity.badRequest().body("No files were found in this path.");

        FileSystemItem item = service.saveFileFromPath(originalPath, folder.get());
        return ResponseEntity.ok("File Received: " + item.getName() + " (" + item.getSize() + " bytes)");
    }

    @PostMapping("/createFolder")
    public ResponseEntity<String> createFolder(@RequestParam String name, Long idFolder) throws IOException {
        Optional<Folder> folder = service.findFolderById(idFolder);

        if (folder.isEmpty())
            return ResponseEntity.badRequest().body("There is no folder with the id set");

        FileSystemItem item = service.createFolder(name, folder.get());

        return ResponseEntity.ok("Folder Created: " + item.getName() + " (" + item.getPath() + ")");
    }

    @GetMapping("/getContentsFromFolder")
    public ResponseEntity<?> getContentsFromFolder(@RequestParam Long idFolder) throws IOException {
        Optional<Folder> folder = service.findFolderById(idFolder);
        if (folder.isEmpty())
            return ResponseEntity.badRequest().body("There is no folder with the id set");

        List<FileSystemItem> contents = service.getContentsFromFolder(folder.get());
        return ResponseEntity.ok(contents);
    }

    @DeleteMapping("/deleteFileByID")
    public ResponseEntity<Void> deleteFileByID(@RequestParam Long id, boolean fullDelete) {
        boolean result = service.deleteFileById(id, fullDelete);

        if (result) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/deleteFolderByID")
    public ResponseEntity<Void> deleteFolderByID(@RequestParam Long id, boolean fullDelete) {
        boolean result = service.deleteFolderById(id, fullDelete);

        if (result) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadItem(@PathVariable Long id) {
        try {
            Resource resource = service.downloadItem(id);
            FileSystemItem item = service.findById(id).orElseThrow();

            String contentType = item instanceof File ?
                    Files.probeContentType(Path.of(((File) item).getHardLinkPath())) :
                    "application/zip";

            String filename = item.getName() + (item instanceof Folder ? ".zip" : "");

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + filename + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    @GetMapping("/getSize/{id}")
    public ResponseEntity<String> getSize(@PathVariable Long id) throws IOException {
        Optional<FileSystemItem> item = service.findById(id);
        if (item.isEmpty())
            return ResponseEntity.badRequest().body("File not Found");

        return ResponseEntity.ok(item.get().getSize().toString());
    }
}