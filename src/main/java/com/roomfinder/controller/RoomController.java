package com.roomfinder.controller;

import com.roomfinder.dto.request.RoomRequest;
import com.roomfinder.entity.Room;
import com.roomfinder.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

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
            @Valid @ModelAttribute RoomRequest request,
            @RequestHeader("X-Landlord-Id") Long landlordId) {
        Room room = roomService.updateRoom(id, request, landlordId);
        return ResponseEntity.ok(room);
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
    public ResponseEntity<Void> toggleAvailability(@PathVariable Long id) {
        roomService.toggleAvailability(id);
        return ResponseEntity.ok().build();
    }
}