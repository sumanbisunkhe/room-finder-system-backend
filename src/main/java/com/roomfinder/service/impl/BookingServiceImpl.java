package com.roomfinder.service.impl;

import com.roomfinder.dto.request.BookingRequest;
import com.roomfinder.entity.Booking;
import com.roomfinder.enums.BookingStatus;
import com.roomfinder.exceptions.BookingNotFoundException;
import com.roomfinder.exceptions.InvalidBookingException;
import com.roomfinder.repository.BookingRepository;
import com.roomfinder.service.BookingService;
import com.roomfinder.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final RoomService roomService;


    @Override
    @Transactional
    public Booking createBooking(BookingRequest request, Long seekerId) {
        validateBookingDates(request.getStartDate(), request.getEndDate());
        validateNoOverlappingBookings(request.getRoomId(), request.getStartDate(), request.getEndDate());

        Booking booking = new Booking();
        booking.setRoomId(request.getRoomId());
        booking.setSeekerId(seekerId);
        booking.setStartDate(request.getStartDate());
        booking.setEndDate(request.getEndDate());
        booking.setComments(request.getComments());

        return bookingRepository.save(booking);
    }

    @Override
    @Transactional
    public Booking approveBooking(Long bookingId, Long landlordId) {
        Booking booking = getBookingById(bookingId);
        validateBookingStatus(booking, BookingStatus.PENDING);

        // Verify that the landlord owns the room
        if (!roomService.isRoomOwner(booking.getRoomId(), landlordId)) {
            throw new AccessDeniedException("Only the room owner can approve bookings");
        }

        validateNoOverlappingBookings(booking.getRoomId(), booking.getStartDate(), booking.getEndDate());

        booking.setStatus(BookingStatus.APPROVED);
        return bookingRepository.save(booking);
    }

    @Override
    @Transactional
    public Booking rejectBooking(Long bookingId, Long landlordId) {
        Booking booking = getBookingById(bookingId);
        validateBookingStatus(booking, BookingStatus.PENDING);

        if (!roomService.isRoomOwner(booking.getRoomId(), landlordId)) {
            throw new AccessDeniedException("Only the room owner can reject bookings");
        }

        booking.setStatus(BookingStatus.REJECTED);
        return bookingRepository.save(booking);
    }

    @Override
    @Transactional
    public Booking cancelBooking(Long bookingId, Long seekerId) {
        Booking booking = getBookingById(bookingId);

        if (!booking.getSeekerId().equals(seekerId)) {
            throw new AccessDeniedException("Only the booking creator can cancel the booking");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new InvalidBookingException("Booking is already cancelled");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        return bookingRepository.save(booking);
    }

    @Override
    @Transactional
    public Booking updateBooking(Long bookingId, BookingRequest request, Long seekerId) {
        Booking booking = getBookingById(bookingId);

        if (!booking.getSeekerId().equals(seekerId)) {
            throw new AccessDeniedException("Only the booking creator can update the booking");
        }

        validateBookingStatus(booking, BookingStatus.PENDING);
        validateBookingDates(request.getStartDate(), request.getEndDate());

        if (!booking.getStartDate().equals(request.getStartDate()) ||
                !booking.getEndDate().equals(request.getEndDate())) {
            validateNoOverlappingBookings(booking.getRoomId(), request.getStartDate(), request.getEndDate());
        }

        booking.setStartDate(request.getStartDate());
        booking.setEndDate(request.getEndDate());
        booking.setComments(request.getComments());

        return bookingRepository.save(booking);
    }

    @Override
    public Booking getBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with id: " + bookingId));
    }

    @Override
    public List<Booking> getBookingsBySeeker(Long seekerId) {
        return bookingRepository.findBySeekerId(seekerId);
    }

    @Override
    public List<Booking> getBookingsByRoom(Long roomId) {
        return bookingRepository.findByRoomId(roomId);
    }

    @Override
    public List<Booking> getPendingBookingsByRoom(Long roomId) {
        return bookingRepository.findByRoomIdAndStatus(roomId, BookingStatus.PENDING);
    }

    private void validateBookingDates(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new InvalidBookingException("Start date must be before end date");
        }

        if (startDate.isBefore(LocalDate.now())) {
            throw new InvalidBookingException("Start date cannot be in the past");
        }
    }

    private void validateNoOverlappingBookings(Long roomId, LocalDate startDate, LocalDate endDate) {
        List<Booking> overlappingBookings = bookingRepository
                .findOverlappingBookings(roomId, startDate, endDate);

        if (!overlappingBookings.isEmpty()) {
            throw new InvalidBookingException("Room is already booked for the selected dates");
        }
    }

    private void validateBookingStatus(Booking booking, BookingStatus expectedStatus) {
        if (booking.getStatus() != expectedStatus) {
            throw new InvalidBookingException(
                    "Invalid booking status. Expected: " + expectedStatus +
                            ", Current: " + booking.getStatus());
        }
    }
}