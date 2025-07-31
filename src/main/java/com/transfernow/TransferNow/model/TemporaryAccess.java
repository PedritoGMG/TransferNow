package com.transfernow.TransferNow.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class TemporaryAccess {
    private String path;
    private Instant expiration;
}