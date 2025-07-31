package com.transfernow.TransferNow.model;

import lombok.Data;

import java.util.UUID;

@Data
public class FileRequest {
    private String requestId = UUID.randomUUID().toString();
    private String fileName;
    private String fileType;
    private String mimeType;
    private long fileSize;
    private String senderName;
    private String fileHash;
    private String senderSessionId;
}
