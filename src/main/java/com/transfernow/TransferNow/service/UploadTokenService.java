package com.transfernow.TransferNow.service;

import com.transfernow.TransferNow.model.FileRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UploadTokenService {
    private final Map<String, FileRequest> pendingRequests = new ConcurrentHashMap<>();
    private final Map<String, String> tokens = new ConcurrentHashMap<>();
    private final Map<String, String> requestToSession = new ConcurrentHashMap<>();
    private static final int MAX_PENDING_REQUESTS = 50;

    public void addPendingRequest(FileRequest request) {
        if (pendingRequests.size() >= MAX_PENDING_REQUESTS) {
            throw new IllegalStateException("Maximum number of pending requests reached");
        }
        pendingRequests.put(request.getRequestId(), request);
        requestToSession.put(request.getRequestId(), request.getSenderSessionId());
    }

    public FileRequest getRequest(String requestId) {
        return pendingRequests.get(requestId);
    }

    public FileRequest getAndRemoveRequest(String requestId) {
        FileRequest request = pendingRequests.remove(requestId);
        requestToSession.remove(requestId);
        return request;
    }

    public void addToken(String requestId, String token) {
        tokens.put(token, requestId);
    }

    public boolean isValidToken(String requestId, String token) {
        String storedRequestId = tokens.get(token);
        return storedRequestId != null && storedRequestId.equals(requestId);
    }

    public boolean isValidTokenForSession(String requestId, String token, String sessionId) {
        String storedRequestId = tokens.get(token);
        String expectedSessionId = requestToSession.get(requestId);

        return storedRequestId != null &&
                storedRequestId.equals(requestId) &&
                expectedSessionId != null &&
                expectedSessionId.equals(sessionId);
    }

    public void removeToken(String token) {
        tokens.remove(token);
    }

    public void removeFull(String requestId) {
        pendingRequests.remove(requestId);
        requestToSession.remove(requestId);
        tokens.entrySet().removeIf(entry -> entry.getValue().equals(requestId));
    }
    public List<FileRequest> getPendingRequests() {
        return pendingRequests.values().stream().toList();
    }
}
