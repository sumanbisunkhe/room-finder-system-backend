package com.roomfinder.service.impl;

import com.roomfinder.dto.request.RoomRequest;
import com.roomfinder.entity.Room;
import com.roomfinder.exceptions.RoomNotFoundException;
import com.roomfinder.exceptions.UnauthorizedAccessException;
import com.roomfinder.repository.RoomRepository;
import com.roomfinder.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;

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
    public Room updateRoom(Long roomId, RoomRequest request, Long landlordId) {
        Room room = getRoomById(roomId);

        if (!room.getLandlordId().equals(landlordId)) {
            throw new UnauthorizedAccessException("You are not authorized to update this room.");
        }

        updateRoomFromRequest(room, request);

        // Handle new image uploads
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            List<String> imagePaths = new ArrayList<>(room.getImages());
            for (MultipartFile image : request.getImages()) {
                String imagePath = saveImage(image);
                imagePaths.add(imagePath);
            }
            room.setImages(imagePaths);
        }

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
                // Log error but continue deletion
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
    public List<Room> getRoomsByLandlord(Long landlordId) {
        return roomRepository.findByLandlordId(landlordId);
    }

    @Override
    public List<Room> searchRooms(String city, Double maxPrice, String address) {
        if (address != null && !address.isEmpty()) {
            return roomRepository.findRoomsBySimilarAddress(address);
        }
        if (maxPrice != null) {
            return roomRepository.findAvailableRoomsByMaxPrice(maxPrice);
        }
        return roomRepository.findByCityAndIsAvailableTrue(city);
    }


    @Override
    @Transactional
    public void toggleAvailability(Long roomId) {
        Room room = getRoomById(roomId);
        room.setAvailable(!room.isAvailable());
        roomRepository.save(room);
    }

    @Override
    public String saveImage(MultipartFile file) {
        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(filename);

            // Save file
            Files.copy(file.getInputStream(), filePath);

            return filePath.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
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