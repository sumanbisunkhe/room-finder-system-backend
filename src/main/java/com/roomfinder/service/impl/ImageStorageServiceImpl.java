package com.roomfinder.service.impl;

import com.roomfinder.service.ImageStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ImageStorageServiceImpl implements ImageStorageService {

    @Value("${uploads.dir:${user.home}/roomfinder/uploads}")
    private String uploadDirectory;

    public List<String> saveImages(List<MultipartFile> images) throws IOException {
        List<String> filePaths = new ArrayList<>();
        Path uploadPath = Paths.get(uploadDirectory);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        for (MultipartFile image : images) {
            String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(image.getInputStream(), filePath);
            filePaths.add(fileName);
        }

        return filePaths;
    }

    public void deleteImage(String fileName) throws IOException {
        Path filePath = Paths.get(uploadDirectory).resolve(fileName);
        Files.deleteIfExists(filePath);
    }
}