package com.transfernow.TransferNow.service;

import com.transfernow.TransferNow.model.TemporaryAccess;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TemporaryLinkService {

    private final Map<String, TemporaryAccess> activeTokens = new ConcurrentHashMap<>();

    public String generateToken(String endpointPath, Duration duration) {
        String token = UUID.randomUUID().toString();
        activeTokens.put(token, new TemporaryAccess(
                endpointPath,
                Instant.now().plus(duration)
        ));
        return token;
    }

    public boolean isValidToken(String token, String requestedPath) {
        TemporaryAccess access = activeTokens.get(token);
        return access != null &&
                !access.getExpiration().isBefore(Instant.now()) &&
                access.getPath().equals(requestedPath);
    }

    public String getOriginalPath(String token) {
        TemporaryAccess access = activeTokens.get(token);
        return (access != null && !access.getExpiration().isBefore(Instant.now())) ?
                access.getPath() : null;
    }

    @Scheduled(fixedRate = 3600000)
    public void cleanExpiredTokens() {
        activeTokens.entrySet().removeIf(entry ->
                entry.getValue().getExpiration().isBefore(Instant.now())
        );
    }
}