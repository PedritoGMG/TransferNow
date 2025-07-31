package com.transfernow.TransferNow.controller;

import com.transfernow.TransferNow.service.TemporaryLinkService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Duration;

@RestController
@RequestMapping("/api/links")
public class LinkController {

    private final TemporaryLinkService linkService;

    public LinkController(TemporaryLinkService linkService) {
        this.linkService = linkService;
    }

    @PostMapping("/generate")
    public ResponseEntity<String> generateTemporaryLink(
            @RequestParam String endpointPath,
            @RequestParam(defaultValue = "24") int hours) {

        if (!endpointPath.startsWith("/api/")) {
            endpointPath = "/api" + endpointPath;
        }

        String token = linkService.generateToken(
                endpointPath,
                Duration.ofHours(hours)
        );

        return ResponseEntity.ok("/api/temp" + endpointPath.substring(4) + "?token=" + token);
    }
}
