package com.roomfinder.service.impl;

import com.roomfinder.dto.request.RoomRequest;
import com.roomfinder.entity.Room;
import com.roomfinder.exceptions.ResourceNotFoundException;
import com.roomfinder.exceptions.RoomNotFoundException;
import com.roomfinder.exceptions.UnauthorizedAccessException;
import com.roomfinder.repository.RoomRepository;
import com.roomfinder.service.ImageStorageService;
import com.roomfinder.service.RoomService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final ImageStorageService imageStorageService;

    @Value("${app.upload.dir:${user.home}/roomfinder/uploads}")
    private String uploadDir;

    @Override
    @Transactional
    public Room createRoom(RoomRequest request, Long landlordId) {
        Room room = new Room();
        room.setLandlordId(landlordId);
        updateRoomFromRequest(room, request);

        // Handle image uploads
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            List<String> imagePaths = new ArrayList<>();
            for (MultipartFile image : request.getImages()) {
                String imagePath = saveImage(image);
                imagePaths.add(imagePath);
            }
            room.setImages(imagePaths);
        }

        return roomRepository.save(room);
    }

    @Override
    @Transactional
    public Room updateRoom(Long roomId, RoomRequest request, List<String> existingImages, Long landlordId) throws IOException {
        Room room = getRoomById(roomId);

        if (!room.getLandlordId().equals(landlordId)) {
            throw new UnauthorizedAccessException("You are not authorized to update this room.");
        }

        updateRoomFromRequest(room, request);

        List<String> imagePaths = new ArrayList<>(existingImages);

        if (request.getImages() != null && !request.getImages().isEmpty()) {
            List<String> newImagePaths = imageStorageService.saveImages(request.getImages());
            imagePaths.addAll(newImagePaths);
        }

        room.setImages(imagePaths);
        return roomRepository.save(room);
    }


    @Override
    public void deleteRoom(Long roomId, Long landlordId) {
        Room room = getRoomById(roomId);

        if (!room.getLandlordId().equals(landlordId)) {
            throw new UnauthorizedAccessException("You are not authorized to delete this room.");
        }

        // Delete images from the filesystem
        for (String imagePath : room.getImages()) {
            try {
                Files.deleteIfExists(Paths.get(imagePath));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        roomRepository.deleteById(roomId);
    }


    @Override
    public Room getRoomById(Long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException("Room not found with id: " + roomId));
    }

    @Override
    public Page<Room> getRoomsByLandlord(Long landlordId, Pageable pageable) {
        return roomRepository.findByLandlordId(landlordId, pageable);
    }

    @Override
    public Page<Room> getAllRooms(Pageable pageable) {
        return roomRepository.findAll(pageable);
    }

    @Override
    public Page<Room> searchRooms(String city, Double maxPrice, String address, Pageable pageable) {
        if (address != null && !address.isEmpty()) {
            return roomRepository.findRoomsBySimilarAddress(address, pageable);
        }
        if (maxPrice != null) {
            return roomRepository.findAvailableRoomsByMaxPrice(maxPrice, pageable);
        }
        return roomRepository.findByCityAndAvailableTrue(city, pageable);
    }


    @Override
    @Transactional
    public void toggleAvailability(Long roomId, Long landlordId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Room not found with id: " + roomId));

        // Verify landlord ownership
        if (!room.getLandlordId().equals(landlordId)) {
            throw new AccessDeniedException("Unauthorized access to modify room");
        }

        room.setAvailable(!room.isAvailable());
        roomRepository.save(room);
    }

    @Override
    public void setAvailability(Long roomId, Long landlordId, boolean available) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Room not found"));

        if (!room.getLandlordId().equals(landlordId)) {
            throw new AccessDeniedException("Unauthorized access to modify room");
        }

        room.setAvailable(available);
        roomRepository.save(room);
    }

    @Override
    public String saveImage(MultipartFile file) {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename = file.getOriginalFilename();
            String filename = UUID.randomUUID() + "_" + (originalFilename != null ? originalFilename : "image") + ".jpg";
            Path filePath = uploadPath.resolve(filename);

            // Read image using TwelveMonkeys plugin-enhanced ImageIO
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                throw new RuntimeException("Unsupported image format");
            }

            // Convert images with transparency to RGB
            if (image.getTransparency() != Transparency.OPAQUE) {
                BufferedImage newImage = new BufferedImage(
                        image.getWidth(),
                        image.getHeight(),
                        BufferedImage.TYPE_INT_RGB
                );
                Graphics2D g = newImage.createGraphics();
                g.drawImage(image, 0, 0, null);
                g.dispose();
                image = newImage;
            }

            // Resize large images while maintaining aspect ratio
            int maxWidth = 1024;
            int originalWidth = image.getWidth();
            int originalHeight = image.getHeight();
            if (originalWidth > maxWidth) {
                double ratio = (double) maxWidth / originalWidth;
                int newHeight = (int) (originalHeight * ratio);
                BufferedImage resizedImage = new BufferedImage(
                        maxWidth,
                        newHeight,
                        BufferedImage.TYPE_INT_RGB
                );
                Graphics2D g = resizedImage.createGraphics();
                g.setRenderingHint(
                        RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BILINEAR
                );
                g.drawImage(image, 0, 0, maxWidth, newHeight, null);
                g.dispose();
                image = resizedImage;
            }

            // Configure high-quality JPEG encoding
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("JPEG");
            if (!writers.hasNext()) {
                throw new RuntimeException("JPEG writer not available");
            }
            ImageWriter writer = writers.next();
            ImageWriteParam param = writer.getDefaultWriteParam();

            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(0.85f);  // 85% quality for good balance

            try (FileImageOutputStream output = new FileImageOutputStream(filePath.toFile())) {
                writer.setOutput(output);
                writer.write(null, new IIOImage(image, null, null), param);
            } finally {
                writer.dispose();
            }

            return filename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to process   and store image", e);
        }
    }

    @Override
    public boolean isRoomOwner(Long roomId, Long landlordId) {
        return roomRepository.findByIdAndLandlordId(roomId, landlordId).isPresent();
    }

    @Override
    public Long getRoomOwnerId(Long roomId) {
        return roomRepository.findById(roomId)
                .map(Room::getLandlordId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + roomId));
    }

    @Override
    public List<Long> getRoomIdsByLandlordId(Long landlordId) {
        return roomRepository.findRoomIdsByLandlordId(landlordId);
    }

    @Override
    public Page<Room> getNewListingsLast7Days(Pageable pageable) {
        LocalDateTime date = LocalDateTime.now().minusDays(7);
        return roomRepository.findNewListingsSince(date, pageable);
    }

    @Override
    public Double getAveragePriceLast7Days() {
        LocalDateTime date = LocalDateTime.now().minusDays(7);
        return roomRepository.findAveragePriceSince(date);
    }

    @Override
    public Map<String, Object> getNewListingsStatsLast7Days() {
        LocalDateTime date = LocalDateTime.now().minusDays(7);
        Double avgPrice = roomRepository.findAveragePriceSince(date);
        List<Object[]> cityStats = roomRepository.findAveragePriceByCitySince(date);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalAveragePrice", avgPrice);

        List<Map<String, Object>> cityData = cityStats.stream()
                .map(arr -> {
                    Map<String, Object> cityMap = new HashMap<>();
                    cityMap.put("city", arr[0]);
                    cityMap.put("averagePrice", arr[1]);
                    cityMap.put("listingCount", arr[2]);
                    return cityMap;
                })
                .collect(Collectors.toList());

        stats.put("cities", cityData);
        return stats;
    }

    @Override
    public Map<String, Object> getPropertyStatusStats() {
        Map<String, Object> stats = new LinkedHashMap<>();

        Long totalProperties = roomRepository.countAllRooms();
        Long availableProperties = roomRepository.countAvailableRooms();
        Long occupiedProperties = roomRepository.countOccupiedRooms();
        Double avgPrice = roomRepository.getAveragePrice();

        stats.put("totalProperties", totalProperties);
        stats.put("availableProperties", availableProperties);
        stats.put("occupiedProperties", occupiedProperties);
        stats.put("occupancyRate", totalProperties == 0 ? 0 :
                (double) occupiedProperties / totalProperties * 100);
        stats.put("averagePrice", avgPrice != null ? avgPrice : 0);

        return stats;
    }

    @Override
    public Map<String, Long> getPriceRangeDistribution() {
        List<Object[]> results = roomRepository.countRoomsByPriceRange();
        Map<String, Long> distribution = new LinkedHashMap<>();

        // Initialize all ranges with 0
        distribution.put("0-5000", 0L);
        distribution.put("5000-10000", 0L);
        distribution.put("10000-15000", 0L);
        distribution.put("15000-20000", 0L);
        distribution.put("20000+", 0L);

        // Update with actual values
        for (Object[] result : results) {
            distribution.put((String) result[0], (Long) result[1]);
        }

        return distribution;
    }

    @Override
    public Map<String, Long> getCityDistribution() {
        List<Object[]> results = roomRepository.countRoomsByCity();
        return results.stream()
                .collect(Collectors.toMap(
                        arr -> (String) arr[0],
                        arr -> (Long) arr[1],
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }


    private void updateRoomFromRequest(Room room, RoomRequest request) {
        room.setTitle(request.getTitle());
        room.setDescription(request.getDescription());
        room.setPrice(request.getPrice());
        room.setAddress(request.getAddress());
        room.setCity(request.getCity());
        room.setSize(request.getSize());
        if (request.getAmenities() != null) {
            room.setAmenities(request.getAmenities());
        }
    }
}
