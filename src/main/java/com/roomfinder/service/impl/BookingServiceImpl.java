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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
        booking.setStatus(BookingStatus.PENDING);

        return bookingRepository.save(booking);
    }

    @Override
    @Transactional
    public Booking approveBooking(Long bookingId, Long landlordId) {
        Booking booking = getBookingById(bookingId);
        validateBookingStatus(booking, BookingStatus.PENDING);

        if (!roomService.isRoomOwner(booking.getRoomId(), landlordId)) {
            throw new AccessDeniedException("Only the room owner can approve bookings");
        }

        validateNoOverlappingBookings(booking.getRoomId(), booking.getStartDate(), booking.getEndDate());

        booking.setStatus(BookingStatus.APPROVED);
        roomService.setAvailability(booking.getRoomId(), landlordId, false);
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
    @Transactional
    public void deleteBooking(Long bookingId, Long userId) {
        Booking booking = getBookingById(bookingId);

        boolean isSeeker = booking.getSeekerId().equals(userId);
        boolean isLandlord = roomService.isRoomOwner(booking.getRoomId(), userId);

        if (!isSeeker && !isLandlord) {
            throw new AccessDeniedException("Only the booking creator or room owner can delete the booking");
        }

        if (booking.getStatus() == BookingStatus.APPROVED) {
            throw new InvalidBookingException("Cannot delete an approved booking. Please cancel it first.");
        }

        bookingRepository.delete(booking);
    }

    @Override
    public Booking getBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with id: " + bookingId));
    }

    @Override
    public Page<Booking> getBookingsBySeeker(Long seekerId, Pageable pageable) {
        return bookingRepository.findBySeekerIdOrderByIdDesc(seekerId, pageable);
    }

    @Override
    public Page<Booking> getBookingsByRoom(Long roomId, Pageable pageable) {
        return bookingRepository.findByRoomIdOrderByIdDesc(roomId, pageable);
    }

    @Override
    public Page<Booking> getPendingBookingsByRoom(Long roomId, Pageable pageable) {
        return bookingRepository.findByRoomIdAndStatusOrderByIdDesc(roomId, BookingStatus.PENDING, pageable);
    }

    @Override
    public Page<Booking> getBookingsByLandlord(Long landlordId, Pageable pageable) {
        List<Long> roomIds = roomService.getRoomIdsByLandlordId(landlordId);
        if (roomIds.isEmpty()) {
            return Page.empty();
        }
        return bookingRepository.findByRoomIdInOrderByIdDesc(roomIds, pageable);
    }

    @Override
    public Page<Booking> getBookingsByStatus(BookingStatus status, Pageable pageable) {
        return bookingRepository.findByStatusOrderByIdDesc(status, pageable);
    }

    @Override
    public Page<Booking> getBookingsBySeekerAndStatus(Long seekerId, BookingStatus status, Pageable pageable) {
        return bookingRepository.findBySeekerIdAndStatusOrderByIdDesc(seekerId, status, pageable);
    }

    @Override
    public Page<Booking> getBookingsByRoomAndStatus(Long roomId, BookingStatus status, Pageable pageable) {
        return bookingRepository.findByRoomIdAndStatusOrderByIdDesc(roomId, status, pageable);
    }

    @Override
    public Page<Booking> getBookingsByLandlordAndStatus(Long landlordId, BookingStatus status, Pageable pageable) {
        List<Long> roomIds = roomService.getRoomIdsByLandlordId(landlordId);
        if (roomIds.isEmpty()) {
            return Page.empty();
        }
        return bookingRepository.findByRoomIdInAndStatusOrderByIdDesc(roomIds, status, pageable);
    }

    @Override
    public Page<Booking> getBookingsBySeekerAndRoom(Long seekerId, Long roomId, Pageable pageable) {
        return bookingRepository.findBySeekerIdAndRoomIdOrderByIdDesc(seekerId, roomId, pageable);
    }

    @Override
    public Page<Booking> getBookingsBySeekerAndRoomAndStatus(Long seekerId, Long roomId, BookingStatus status, Pageable pageable) {
        return bookingRepository.findBySeekerIdAndRoomIdAndStatusOrderByIdDesc(seekerId, roomId, status, pageable);
    }

    @Override
    public Page<Booking> searchBookings(
            Long seekerId,
            Long roomId,
            BookingStatus status,
            LocalDate startDateFrom,
            LocalDate startDateTo,
            LocalDate endDateFrom,
            LocalDate endDateTo,
            Pageable pageable) {
        return bookingRepository.searchBookings(
                seekerId,
                roomId,
                status,
                startDateFrom,
                startDateTo,
                endDateFrom,
                endDateTo,
                pageable);
    }

    @Override
    public Page<Booking> searchBookingsForLandlord(
            Long landlordId,
            Long seekerId,
            BookingStatus status,
            LocalDate startDateFrom,
            LocalDate startDateTo,
            LocalDate endDateFrom,
            LocalDate endDateTo,
            Pageable pageable) {
        List<Long> roomIds = roomService.getRoomIdsByLandlordId(landlordId);
        if (roomIds.isEmpty()) {
            return Page.empty();
        }
        return bookingRepository.searchBookingsForLandlord(
                roomIds,
                seekerId,
                status,
                startDateFrom,
                startDateTo,
                endDateFrom,
                endDateTo,
                pageable);
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