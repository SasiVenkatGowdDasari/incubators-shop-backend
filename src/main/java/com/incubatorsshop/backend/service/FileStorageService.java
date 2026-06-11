package com.incubatorsshop.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.*;
import java.io.IOException;
import java.util.UUID;

@Service
public class FileStorageService {
    // Create an "uploads" folder in your project root
    private final Path root = Paths.get("uploads");

    public FileStorageService() {
        try { Files.createDirectories(root); } catch (IOException e) { e.printStackTrace(); }
    }

    public String store(MultipartFile file) {
        try {
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Files.copy(file.getInputStream(), this.root.resolve(fileName));
            return fileName;
        } catch (Exception e) {
            throw new RuntimeException("Could not store file.", e);
        }
    }
}