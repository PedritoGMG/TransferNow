package com.transfernow.TransferNow.controller;

import com.transfernow.TransferNow.model.FileRequest;
import com.transfernow.TransferNow.model.FileSystemItem;
import com.transfernow.TransferNow.service.FileSystemItemService;
import com.transfernow.TransferNow.service.FileValidationService;
import com.transfernow.TransferNow.service.UploadTokenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/secure-upload")
public class FileUploadController {
    private final UploadTokenService tokenService;
    private final FileSystemItemService fileSystemItemService;
    private final FileValidationService validationService;

    public FileUploadController(FileSystemItemService fileSystemItemService,
                                UploadTokenService tokenService,
                                FileValidationService validationService) {
        this.fileSystemItemService = fileSystemItemService;
        this.tokenService = tokenService;
        this.validationService = validationService;
    }

    @PostMapping("/{requestId}")
    public ResponseEntity<String> uploadFile(
            @PathVariable String requestId,
            @RequestParam("file") MultipartFile file,
            @RequestHeader("Authorization") String token,
            @RequestParam Long folderId,
            @RequestHeader("X-Session-Id") String sessionId
    ) {

        FileRequest originalRequest = tokenService.getRequest(requestId);

        if (!tokenService.isValidTokenForSession(requestId, token, sessionId) || originalRequest == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        tokenService.removeFull(requestId);

        if (!validationService.validateFile(originalRequest, file)) {
            return ResponseEntity.badRequest().body("The file does not meet the validations");
        }

        FileSystemItem item = fileSystemItemService.saveFile(file, folderId);
        return ResponseEntity.ok("Uploaded File: " + item.getName());
    }
}
