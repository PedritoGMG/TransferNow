package com.transfernow.TransferNow.filter;

import com.transfernow.TransferNow.model.File;
import com.transfernow.TransferNow.model.FileSystemItem;
import com.transfernow.TransferNow.model.Folder;
import com.transfernow.TransferNow.service.FileSystemItemService;
import com.transfernow.TransferNow.service.TemporaryLinkService;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TemporaryAccessFilter extends OncePerRequestFilter {

    private final TemporaryLinkService linkService;
    private final FileSystemItemService fileSystemItemService; // Inyecta el servicio

    public TemporaryAccessFilter(TemporaryLinkService linkService, FileSystemItemService fileSystemItemService) {
        this.linkService = linkService;
        this.fileSystemItemService = fileSystemItemService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws IOException, ServletException {

        String requestURI = request.getRequestURI();

        if (requestURI.startsWith("/api/temp/filesystem/download/")) {
            String token = request.getParameter("token");
            String originalPath = requestURI.replaceFirst("/api/temp", "/api");

            if (token == null || !linkService.isValidToken(token, originalPath)) {
                response.sendError(HttpStatus.FORBIDDEN.value(), "Invalid or expired token");
                return;
            }

            String idStr = requestURI.substring(requestURI.lastIndexOf('/') + 1);
            Long id = Long.parseLong(idStr);

            try {
                Resource resource = fileSystemItemService.downloadItem(id);
                FileSystemItem item = fileSystemItemService.findById(id).orElseThrow();

                String contentType = item instanceof File ?
                        Files.probeContentType(Path.of(((File) item).getHardLinkPath())) :
                        "application/zip";

                String filename = item.getName() + (item instanceof Folder ? ".zip" : "");

                response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"");
                response.setHeader(HttpHeaders.CONTENT_TYPE, contentType);

                try (InputStream inputStream = resource.getInputStream();
                     OutputStream outputStream = response.getOutputStream()) {
                    IOUtils.copy(inputStream, outputStream);
                }
                return;
            } catch (Exception e) {
                response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error downloading file");
                return;
            }
        }

        chain.doFilter(request, response);
    }
}