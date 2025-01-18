package com.roomfinder.service;

import com.roomfinder.dto.request.BookingRequest;
import com.roomfinder.entity.Booking;
import java.util.List;

public interface BookingService {
    Booking createBooking(BookingRequest request, Long seekerId);
    Booking approveBooking(Long bookingId, Long landlordId);
    Booking rejectBooking(Long bookingId, Long landlordId);
    Booking cancelBooking(Long bookingId, Long seekerId);
    Booking updateBooking(Long bookingId, BookingRequest request, Long seekerId);
    Booking getBookingById(Long bookingId);
    List<Booking> getBookingsBySeeker(Long seekerId);
    List<Booking> getBookingsByRoom(Long roomId);
    List<Booking> getPendingBookingsByRoom(Long roomId);
}