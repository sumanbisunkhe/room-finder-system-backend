package com.roomfinder.controller;

import com.opencsv.exceptions.CsvValidationException;
import com.roomfinder.service.CSVService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
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
    public ResponseEntity<InputStreamResource> exportUsers() throws IOException {
        ByteArrayInputStream in = csvService.exportUsersToCSV();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=users.csv");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/csv"))
                .body(new InputStreamResource(in));
    }

    @PostMapping("/import/users")
    public ResponseEntity<String> importUsers(@RequestParam("file") MultipartFile file) {
        try {
            csvService.importUsersFromCSV(file);
            return ResponseEntity.ok("Users imported successfully.");
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Failed to import users: " + e.getMessage());
        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        }
    }

    // ----- ROOM ENDPOINTS -----
    @GetMapping("/export/rooms")
    public ResponseEntity<InputStreamResource> exportRooms() throws IOException {
        ByteArrayInputStream in = csvService.exportRoomsToCSV();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=rooms.csv");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/csv"))
                .body(new InputStreamResource(in));
    }

    @PostMapping("/import/rooms")
    public ResponseEntity<String> importRooms(@RequestParam("file") MultipartFile file) {
        try {
            csvService.importRoomsFromCSV(file);
            return ResponseEntity.ok("Rooms imported successfully.");
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Failed to import rooms: " + e.getMessage());
        }
    }

    // ----- MESSAGE ENDPOINTS -----
    @GetMapping("/export/messages")
    public ResponseEntity<InputStreamResource> exportMessages() throws IOException {
        ByteArrayInputStream in = csvService.exportMessagesToCSV();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=messages.csv");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/csv"))
                .body(new InputStreamResource(in));
    }

    @PostMapping("/import/messages")
    public ResponseEntity<String> importMessages(@RequestParam("file") MultipartFile file) {
        try {
            csvService.importMessagesFromCSV(file);
            return ResponseEntity.ok("Messages imported successfully.");
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Failed to import messages: " + e.getMessage());
        }
    }

    // ----- BOOKING ENDPOINTS -----
    @GetMapping("/export/bookings")
    public ResponseEntity<InputStreamResource> exportBookings() throws IOException {
        ByteArrayInputStream in = csvService.exportBookingsToCSV();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=bookings.csv");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/csv"))
                .body(new InputStreamResource(in));
    }

    @PostMapping("/import/bookings")
    public ResponseEntity<String> importBookings(@RequestParam("file") MultipartFile file) {
        try {
            csvService.importBookingsFromCSV(file);
            return ResponseEntity.ok("Bookings imported successfully.");
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Failed to import bookings: " + e.getMessage());
        }
    }
}
