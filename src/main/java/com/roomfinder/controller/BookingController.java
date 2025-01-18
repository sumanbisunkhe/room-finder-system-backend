package com.roomfinder.controller;

import com.roomfinder.dto.request.BookingRequest;
import com.roomfinder.entity.Booking;
import com.roomfinder.security.CustomUserDetails;
import com.roomfinder.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private Long getCurrentUserId(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getId();
    }

    @PostMapping
    public ResponseEntity<Booking> createBooking(
            @Valid @RequestBody BookingRequest request,
            Authentication authentication) {
        Long seekerId = getCurrentUserId(authentication);
        Booking booking = bookingService.createBooking(request, seekerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(booking);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Booking> updateBooking(
            @PathVariable Long id,
            @Valid @RequestBody BookingRequest request,
            Authentication authentication) {
        Long seekerId = getCurrentUserId(authentication);
        Booking booking = bookingService.updateBooking(id, request, seekerId);
        return ResponseEntity.ok(booking);
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<Booking> approveBooking(
            @PathVariable Long id,
            Authentication authentication) {
        Long landlordId = getCurrentUserId(authentication);
        Booking booking = bookingService.approveBooking(id, landlordId);
        return ResponseEntity.ok(booking);
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<Booking> rejectBooking(
            @PathVariable Long id,
            Authentication authentication) {
        Long landlordId = getCurrentUserId(authentication);
        Booking booking = bookingService.rejectBooking(id, landlordId);
        return ResponseEntity.ok(booking);
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Booking> cancelBooking(
            @PathVariable Long id,
            Authentication authentication) {
        Long seekerId = getCurrentUserId(authentication);
        Booking booking = bookingService.cancelBooking(id, seekerId);
        return ResponseEntity.ok(booking);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Booking> getBooking(@PathVariable Long id) {
        Booking booking = bookingService.getBookingById(id);
        return ResponseEntity.ok(booking);
    }

    @GetMapping("/seeker/{seekerId}")
    public ResponseEntity<List<Booking>> getBookingsBySeeker(@PathVariable Long seekerId) {
        List<Booking> bookings = bookingService.getBookingsBySeeker(seekerId);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<Booking>> getBookingsByRoom(@PathVariable Long roomId) {
        List<Booking> bookings = bookingService.getBookingsByRoom(roomId);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/room/{roomId}/pending")
    public ResponseEntity<List<Booking>> getPendingBookingsByRoom(@PathVariable Long roomId) {
        List<Booking> bookings = bookingService.getPendingBookingsByRoom(roomId);
        return ResponseEntity.ok(bookings);
    }
}