package com.transfernow.TransferNow.controller;

import com.transfernow.TransferNow.model.FileRequest;
import com.transfernow.TransferNow.service.UploadTokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class FileTransferController {
    private final SimpMessagingTemplate messagingTemplate;
    private final UploadTokenService tokenService;

    public FileTransferController(SimpMessagingTemplate messagingTemplate, UploadTokenService tokenService) {
        this.messagingTemplate = messagingTemplate;
        this.tokenService = tokenService;
    }

    @GetMapping("/api/getRequests")
    public ResponseEntity<List<FileRequest>> getPendingRequests() {
        List<FileRequest> pendingRequests = tokenService.getPendingRequests();
        return ResponseEntity.ok(pendingRequests);
    }

    @MessageMapping("/request/initiate")
    public void initiateRequest(@Payload FileRequest request, @Header("simpSessionId") String sessionId) {

        request.setSenderSessionId(sessionId);
        try {
            tokenService.addPendingRequest(request);
        } catch (IllegalStateException e) {
            messagingTemplate.convertAndSendToUser(
                    "user-" + sessionId,
                    "/queue/rejected",
                    "REJECTED"
            );
            return;
        }

        messagingTemplate.convertAndSend("/queue/host-requests", request);
    }


    @MessageMapping("/request/respond")
    public void handleResponse(@Payload String response, @Header("requestId") String requestId) {
        if ("ACCEPT".equals(response)) {
            FileRequest originalRequest = tokenService.getRequest(requestId);
            if (originalRequest != null) {
                String token = UUID.randomUUID().toString();
                tokenService.addToken(requestId, token);

                messagingTemplate.convertAndSendToUser(
                        "user-" + originalRequest.getSenderSessionId(),
                        "/queue/upload-token",
                        token
                );
            }
        } else {
            FileRequest originalRequest = tokenService.getRequest(requestId);
            tokenService.removeFull(requestId);

            if (originalRequest != null) {
                messagingTemplate.convertAndSendToUser(
                        "user-" + originalRequest.getSenderSessionId(),
                        "/queue/rejected",
                        "REJECTED"
                );
            }
        }
    }
}