package com.roomfinder.controller;

import com.roomfinder.dto.request.BookingRequest;
import com.roomfinder.entity.Booking;
import com.roomfinder.enums.BookingStatus;
import com.roomfinder.security.CustomUserDetails;
import com.roomfinder.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        bookingService.deleteBooking(id, userId);
        return ResponseEntity.noContent().build();
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

    @GetMapping("/landlord/my-bookings")
    public ResponseEntity<Page<Booking>> getBookingsByLandlord(
            Authentication authentication,
            @PageableDefault(size = 10) Pageable pageable) {
        Long landlordId = getCurrentUserId(authentication);
        Page<Booking> bookings = bookingService.getBookingsByLandlord(landlordId, pageable);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/seeker/my-bookings")
    public ResponseEntity<Page<Booking>> getBookingsByCurrentSeeker(
            Authentication authentication,
            @PageableDefault(size = 10) Pageable pageable) {
        Long seekerId = getCurrentUserId(authentication);
        Page<Booking> bookings = bookingService.getBookingsBySeeker(seekerId, pageable);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<Page<Booking>> getBookingsByRoom(
            @PathVariable Long roomId,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<Booking> bookings = bookingService.getBookingsByRoom(roomId, pageable);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/room/{roomId}/pending")
    public ResponseEntity<Page<Booking>> getPendingBookingsByRoom(
            @PathVariable Long roomId,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<Booking> bookings = bookingService.getPendingBookingsByRoom(roomId, pageable);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<Booking>> getBookingsByStatus(
            @PathVariable BookingStatus status,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<Booking> bookings = bookingService.getBookingsByStatus(status, pageable);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/seeker/my-bookings/status/{status}")
    public ResponseEntity<Page<Booking>> getBookingsByCurrentSeekerAndStatus(
            Authentication authentication,
            @PathVariable BookingStatus status,
            @PageableDefault(size = 10) Pageable pageable) {
        Long seekerId = getCurrentUserId(authentication);
        Page<Booking> bookings = bookingService.getBookingsBySeekerAndStatus(seekerId, status, pageable);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/room/{roomId}/status/{status}")
    public ResponseEntity<Page<Booking>> getBookingsByRoomAndStatus(
            @PathVariable Long roomId,
            @PathVariable BookingStatus status,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<Booking> bookings = bookingService.getBookingsByRoomAndStatus(roomId, status, pageable);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/landlord/my-bookings/status/{status}")
    public ResponseEntity<Page<Booking>> getBookingsByLandlordAndStatus(
            Authentication authentication,
            @PathVariable BookingStatus status,
            @PageableDefault(size = 10) Pageable pageable) {
        Long landlordId = getCurrentUserId(authentication);
        Page<Booking> bookings = bookingService.getBookingsByLandlordAndStatus(landlordId, status, pageable);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/seeker/my-bookings/room/{roomId}")
    public ResponseEntity<Page<Booking>> getBookingsByCurrentSeekerAndRoom(
            Authentication authentication,
            @PathVariable Long roomId,
            @PageableDefault(size = 10) Pageable pageable) {
        Long seekerId = getCurrentUserId(authentication);
        Page<Booking> bookings = bookingService.getBookingsBySeekerAndRoom(seekerId, roomId, pageable);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/seeker/my-bookings/room/{roomId}/status/{status}")
    public ResponseEntity<Page<Booking>> getBookingsByCurrentSeekerAndRoomAndStatus(
            Authentication authentication,
            @PathVariable Long roomId,
            @PathVariable BookingStatus status,
            @PageableDefault(size = 10) Pageable pageable) {
        Long seekerId = getCurrentUserId(authentication);
        Page<Booking> bookings = bookingService.getBookingsBySeekerAndRoomAndStatus(seekerId, roomId, status, pageable);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<Booking>> searchBookings(
            Authentication authentication,
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDateTo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDateTo,
            @PageableDefault(size = 10) Pageable pageable) {
        Long seekerId = getCurrentUserId(authentication);
        Page<Booking> bookings = bookingService.searchBookings(
                seekerId,
                roomId,
                status,
                startDateFrom,
                startDateTo,
                endDateFrom,
                endDateTo,
                pageable);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/landlord/search")
    public ResponseEntity<Page<Booking>> searchBookingsForLandlord(
            Authentication authentication,
            @RequestParam(required = false) Long seekerId,
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDateTo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDateTo,
            @PageableDefault(size = 10) Pageable pageable) {
        Long landlordId = getCurrentUserId(authentication);
        Page<Booking> bookings = bookingService.searchBookingsForLandlord(
                landlordId,
                seekerId,
                status,
                startDateFrom,
                startDateTo,
                endDateFrom,
                endDateTo,
                pageable);
        return ResponseEntity.ok(bookings);
    }
}