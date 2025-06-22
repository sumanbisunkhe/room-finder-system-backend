package com.roomfinder.service;

import com.roomfinder.dto.request.BookingRequest;
import com.roomfinder.entity.Booking;
import com.roomfinder.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface BookingService {
    Booking createBooking(BookingRequest request, Long seekerId);

    Booking approveBooking(Long bookingId, Long landlordId);

    Booking rejectBooking(Long bookingId, Long landlordId);

    Booking cancelBooking(Long bookingId, Long seekerId);

    Booking updateBooking(Long bookingId, BookingRequest request, Long seekerId);

    void deleteBooking(Long bookingId, Long userId);

    Booking getBookingById(Long bookingId);

    Page<Booking> getBookingsBySeeker(Long seekerId, Pageable pageable);

    Page<Booking> getBookingsByRoom(Long roomId, Pageable pageable);

    Page<Booking> getPendingBookingsByRoom(Long roomId, Pageable pageable);

    Page<Booking> getBookingsByLandlord(Long landlordId, Pageable pageable);

    Page<Booking> getBookingsByStatus(BookingStatus status, Pageable pageable);

    Page<Booking> getBookingsBySeekerAndStatus(Long seekerId, BookingStatus status, Pageable pageable);

    Page<Booking> getBookingsByRoomAndStatus(Long roomId, BookingStatus status, Pageable pageable);

    Page<Booking> getBookingsByLandlordAndStatus(Long landlordId, BookingStatus status, Pageable pageable);

    // Advanced search methods
    Page<Booking> getBookingsBySeekerAndRoom(Long seekerId, Long roomId, Pageable pageable);

    Page<Booking> getBookingsBySeekerAndRoomAndStatus(Long seekerId, Long roomId, BookingStatus status, Pageable pageable);

    Page<Booking> searchBookings(
            Long seekerId,
            Long roomId,
            BookingStatus status,
            LocalDate startDateFrom,
            LocalDate startDateTo,
            LocalDate endDateFrom,
            LocalDate endDateTo,
            Pageable pageable);

    Page<Booking> searchBookingsForLandlord(
            Long landlordId,
            Long seekerId,
            BookingStatus status,
            LocalDate startDateFrom,
            LocalDate startDateTo,
            LocalDate endDateFrom,
            LocalDate endDateTo,
            Pageable pageable);
}