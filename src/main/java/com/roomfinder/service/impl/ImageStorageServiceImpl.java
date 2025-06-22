package com.roomfinder.service.impl;

import com.roomfinder.service.ImageStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ForkJoinPool;

@Service
public class ImageStorageServiceImpl implements ImageStorageService {

    @Value("${uploads.dir:${user.home}/roomfinder/uploads}")
    private String uploadDirectory;

    private static final int MAX_IMAGE_SIZE = 1024;
    private static final ForkJoinPool IMAGE_PROCESSING_POOL = new ForkJoinPool(Runtime.getRuntime().availableProcessors());

    public List<String> saveImages(List<MultipartFile> images) throws IOException {
        List<String> filePaths = new ArrayList<>();
        Path uploadPath = Paths.get(uploadDirectory);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        IMAGE_PROCESSING_POOL.submit(() ->
                images.parallelStream().forEach(image -> {
                    try {
                        String fileName = processAndSaveImage(image);
                        filePaths.add(fileName);
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to process image", e);
                    }
                })
        ).join();

        return filePaths;
    }

    private String processAndSaveImage(MultipartFile image) throws IOException {
        // Use TwelveMonkeys' optimized ImageReader
        try (ImageInputStream stream = ImageIO.createImageInputStream(image.getInputStream())) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);
            if (!readers.hasNext()) {
                throw new IOException("No reader for image format");
            }

            ImageReader reader = readers.next();
            reader.setInput(stream);

            // Read image metadata first to get dimensions
            int width = reader.getWidth(0);
            int height = reader.getHeight(0);

            // Calculate scaled dimensions
            Dimension scaled = getScaledDimension(width, height);

            // Read and scale image efficiently
            BufferedImage bufferedImage = reader.read(0);
            BufferedImage scaledImage = scaleImage(bufferedImage, scaled.width, scaled.height);

            // Generate filename
            String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
            Path filePath = Paths.get(uploadDirectory).resolve(fileName);

            // Save with optimized compression
            ImageIO.write(scaledImage, "jpg", filePath.toFile());

            return fileName;
        }
    }

    private Dimension getScaledDimension(int width, int height) {
        if (width <= MAX_IMAGE_SIZE && height <= MAX_IMAGE_SIZE) {
            return new Dimension(width, height);
        }

        double ratio = (double) width / height;
        if (width > height) {
            return new Dimension(MAX_IMAGE_SIZE, (int) (MAX_IMAGE_SIZE / ratio));
        }
        return new Dimension((int) (MAX_IMAGE_SIZE * ratio), MAX_IMAGE_SIZE);
    }

    private BufferedImage scaleImage(BufferedImage source, int width, int height) {
        BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(source, 0, 0, width, height, null);
        g.dispose();
        return scaled;
    }

    public void deleteImage(String fileName) throws IOException {
        Path filePath = Paths.get(uploadDirectory).resolve(fileName);
        Files.deleteIfExists(filePath);
    }
}