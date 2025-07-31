package com.transfernow.TransferNow.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

@RestController
public class HostController {
    @GetMapping("/api/host-url")
    public String getHostUrl(HttpServletRequest request) {
        try {
            URL url = new URL("https://checkip.amazonaws.com/");
            try (BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()))) {
                return "http://" + br.readLine() + ":" + 8080;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "unknown";
        }
    }
}
