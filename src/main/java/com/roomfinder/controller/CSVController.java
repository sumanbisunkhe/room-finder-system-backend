package com.roomfinder.controller;

import com.roomfinder.dto.response.ApiResponse;
import com.roomfinder.service.CSVService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@RestController
@RequestMapping("/api/csv")
public class CSVController {

    private final CSVService csvService;

    @Autowired
    public CSVController(CSVService csvService) {
        this.csvService = csvService;
    }

    // ----- USER ENDPOINTS -----
    @GetMapping("/export/users")
    public ResponseEntity<?> exportUsers() {
        try {
            ByteArrayInputStream in = csvService.exportUsersToCSV();
            return getCSVResponseEntity(in, "users.csv");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Failed to export users: " + e.getMessage()));
        }
    }

    @PostMapping("/import/users")
    public ResponseEntity<ApiResponse> importUsers(@RequestParam("file") MultipartFile file) {
        try {
            csvService.importUsersFromCSV(file);
            return ResponseEntity.ok(new ApiResponse(true, "Users imported successfully"));
        } catch (DataIntegrityViolationException e) {
            String errorMessage = e.getRootCause() != null ? e.getRootCause().getMessage() : e.getMessage();
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse(false, "Failed to import users: " + errorMessage));
        } catch (IOException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to import users: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Unexpected error while importing users: " + e.getMessage()));
        }
    }

    // ----- ROOM ENDPOINTS -----
    @GetMapping("/export/rooms")
    public ResponseEntity<?> exportRooms() {
        try {
            ByteArrayInputStream in = csvService.exportRoomsToCSV();
            return getCSVResponseEntity(in, "rooms.csv");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Failed to export rooms: " + e.getMessage()));
        }
    }

    @PostMapping("/import/rooms")
    public ResponseEntity<ApiResponse> importRooms(@RequestParam("file") MultipartFile file) {
        try {
            csvService.importRoomsFromCSV(file);
            return ResponseEntity.ok(new ApiResponse(true, "Rooms imported successfully"));
        } catch (DataIntegrityViolationException e) {
            String errorMessage = e.getRootCause() != null ? e.getRootCause().getMessage() : e.getMessage();
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse(false, "Failed to import rooms: " + errorMessage));
        } catch (IOException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to import rooms: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Unexpected error while importing rooms: " + e.getMessage()));
        }
    }

    // ----- MESSAGE ENDPOINTS -----
    @GetMapping("/export/messages")
    public ResponseEntity<?> exportMessages() {
        try {
            ByteArrayInputStream in = csvService.exportMessagesToCSV();
            return getCSVResponseEntity(in, "messages.csv");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Failed to export messages: " + e.getMessage()));
        }
    }

    @PostMapping("/import/messages")
    public ResponseEntity<ApiResponse> importMessages(@RequestParam("file") MultipartFile file) {
        try {
            csvService.importMessagesFromCSV(file);
            return ResponseEntity.ok(new ApiResponse(true, "Messages imported successfully"));
        } catch (DataIntegrityViolationException e) {
            String errorMessage = e.getRootCause() != null ? e.getRootCause().getMessage() : e.getMessage();
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse(false, "Failed to import messages: " + errorMessage));
        } catch (IOException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to import messages: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Unexpected error while importing messages: " + e.getMessage()));
        }
    }

    // ----- BOOKING ENDPOINTS -----
    @GetMapping("/export/bookings")
    public ResponseEntity<?> exportBookings() {
        try {
            ByteArrayInputStream in = csvService.exportBookingsToCSV();
            return getCSVResponseEntity(in, "bookings.csv");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Failed to export bookings: " + e.getMessage()));
        }
    }

    @PostMapping("/import/bookings")
    public ResponseEntity<ApiResponse> importBookings(@RequestParam("file") MultipartFile file) {
        try {
            csvService.importBookingsFromCSV(file);
            return ResponseEntity.ok(new ApiResponse(true, "Bookings imported successfully"));
        } catch (DataIntegrityViolationException e) {
            String errorMessage = e.getRootCause() != null ? e.getRootCause().getMessage() : e.getMessage();
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse(false, "Failed to import bookings: " + errorMessage));
        } catch (IOException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to import bookings: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Unexpected error while importing bookings: " + e.getMessage()));
        }
    }

    // Helper method for CSV file response
    private ResponseEntity<InputStreamResource> getCSVResponseEntity(ByteArrayInputStream in, String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=" + filename);
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/csv"))
                .body(new InputStreamResource(in));
    }
}