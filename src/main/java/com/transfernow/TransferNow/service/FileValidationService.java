package com.transfernow.TransferNow.service;

import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import com.transfernow.TransferNow.model.FileRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.security.MessageDigest;
import java.nio.file.Paths;


import java.nio.file.Paths;

@Service
public class FileValidationService {

    public boolean validateFile(FileRequest request, MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) return false;
            if (file.getSize() != request.getFileSize()) return false;

            String safeExpectedName = Paths.get(request.getFileName()).getFileName().toString();
            String safeReceivedName = Paths.get(file.getOriginalFilename()).getFileName().toString();
            if (!safeReceivedName.equals(safeExpectedName)) return false;

            String uploadedFileHash = calculateSHA256(file);
            if (!uploadedFileHash.equals(request.getFileHash())) return false;

            Tika tika = new Tika();
            String detectedType = tika.detect(file.getBytes());
            if (!detectedType.equals(request.getMimeType())) return false;

            return true;
        } catch (Exception e) {
            return false;
        }
    }


    private String calculateSHA256(MultipartFile file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream is = file.getInputStream()) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
        }
        return bytesToHex(digest.digest());
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
