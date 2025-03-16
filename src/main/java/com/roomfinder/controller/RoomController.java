package com.roomfinder.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.roomfinder.dto.request.RoomRequest;
import com.roomfinder.entity.Room;
import com.roomfinder.service.RoomService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;
    private final ObjectMapper objectMapper;


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Room> createRoom(
            @Valid @ModelAttribute RoomRequest request,
            @RequestHeader("X-Landlord-Id") Long landlordId) {
        Room room = roomService.createRoom(request, landlordId);
        return ResponseEntity.status(HttpStatus.CREATED).body(room);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Room> updateRoom(
            @PathVariable Long id,
            @RequestPart(value = "existingImages", required = false) String existingImagesJson,
            @Valid @ModelAttribute RoomRequest request,
            @RequestHeader("X-Landlord-Id") Long landlordId) throws IOException {

        // Parse existingImages from JSON string
        List<String> existingImages = new ArrayList<>();
        if (existingImagesJson != null && !existingImagesJson.isEmpty()) {
            existingImages = objectMapper.readValue(existingImagesJson, new TypeReference<List<String>>() {});
        }

        Room updatedRoom = roomService.updateRoom(id, request, existingImages, landlordId);
        return ResponseEntity.ok(updatedRoom);
    }

    @GetMapping
    public ResponseEntity<List<Room>> getAllRooms() {
        List<Room> rooms = roomService.getAllRooms();
        return ResponseEntity.ok(rooms);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(
            @PathVariable Long id,
            @RequestHeader("X-Landlord-Id") Long landlordId) {
        roomService.deleteRoom(id, landlordId);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/{id}")
    public ResponseEntity<Room> getRoom(@PathVariable Long id) {
        Room room = roomService.getRoomById(id);
        return ResponseEntity.ok(room);
    }


    @GetMapping("/landlord/{landlordId}")
    public ResponseEntity<List<Room>> getRoomsByLandlord(@PathVariable Long landlordId) {
        List<Room> rooms = roomService.getRoomsByLandlord(landlordId);
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Room>> searchRooms(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String address) {
        List<Room> rooms = roomService.searchRooms(city, maxPrice, address);
        return ResponseEntity.ok(rooms);
    }


    @PatchMapping("/{id}/availability")
    public ResponseEntity<?> toggleAvailability(
            @PathVariable Long id,
            @RequestHeader("X-Landlord-Id") Long landlordId) {

        try {
            roomService.toggleAvailability(id, landlordId);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        } catch (AccessDeniedException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
        }
    }
}