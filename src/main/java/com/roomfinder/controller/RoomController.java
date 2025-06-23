package com.roomfinder.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.roomfinder.dto.request.RoomRequest;
import com.roomfinder.dto.response.ApiResponse;
import com.roomfinder.entity.Room;
import com.roomfinder.service.RoomService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
            existingImages = objectMapper.readValue(existingImagesJson, new TypeReference<List<String>>() {
            });
        }

        Room updatedRoom = roomService.updateRoom(id, request, existingImages, landlordId);
        return ResponseEntity.ok(updatedRoom);
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
    public ResponseEntity<Page<Room>> getRoomsByLandlord(
            @PathVariable Long landlordId,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(roomService.getRoomsByLandlord(landlordId, pageable));
    }

    @GetMapping
    public ResponseEntity<Page<Room>> getAllRooms(@PageableDefault(size = 5) Pageable pageable) {
        return ResponseEntity.ok(roomService.getAllRooms(pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<Room>> searchRooms(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String address,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(roomService.searchRooms(city, maxPrice, address, pageable));
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

    @GetMapping("/recent/new-listings")
    public ResponseEntity<ApiResponse> getNewListingsLast7Days(
            @PageableDefault(size = 10) Pageable pageable) {
        Page<Room> rooms = roomService.getNewListingsLast7Days(pageable);
        return ResponseEntity.ok(
                new ApiResponse(true, "Recent listings retrieved successfully", rooms)
        );
    }

    @GetMapping("/stats/new-listings")
    public ResponseEntity<ApiResponse> getNewListingsStatsLast7Days() {
        Map<String, Object> stats = roomService.getNewListingsStatsLast7Days();
        return ResponseEntity.ok(
                new ApiResponse(true, "New listings statistics retrieved successfully", stats)
        );
    }
    @GetMapping("/stats/property-status")
    public ResponseEntity<ApiResponse> getPropertyStatusStats() {
        Map<String, Object> stats = roomService.getPropertyStatusStats();
        return ResponseEntity.ok(
                new ApiResponse(true, "Property status statistics retrieved successfully", stats)
        );
    }


    @GetMapping("/stats/price-distribution")
    public ResponseEntity<ApiResponse> getPriceRangeDistribution() {
        Map<String, Long> distribution = roomService.getPriceRangeDistribution();
        return ResponseEntity.ok(
                new ApiResponse(true, "Price distribution statistics retrieved successfully", distribution)
        );
    }

    @GetMapping("/stats/city-distribution")
    public ResponseEntity<ApiResponse> getCityDistribution() {
        Map<String, Long> distribution = roomService.getCityDistribution();
        return ResponseEntity.ok(
                new ApiResponse(true, "City distribution statistics retrieved successfully", distribution)
        );
    }

}