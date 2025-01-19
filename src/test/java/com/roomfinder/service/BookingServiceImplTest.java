package com.roomfinder.service;

import com.roomfinder.dto.request.BookingRequest;
import com.roomfinder.entity.Booking;
import com.roomfinder.enums.BookingStatus;
import com.roomfinder.exceptions.BookingNotFoundException;
import com.roomfinder.exceptions.InvalidBookingException;
import com.roomfinder.repository.BookingRepository;
import com.roomfinder.service.RoomService;
import com.roomfinder.service.impl.BookingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingServiceImplTest {

    @InjectMocks
    private BookingServiceImpl bookingService;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private RoomService roomService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createBooking_ValidData_Success() {
        BookingRequest request = new BookingRequest();
        request.setRoomId(1L);
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(3));
        request.setComments("Test Booking");

        Booking savedBooking = new Booking();
        savedBooking.setId(1L);

        when(bookingRepository.findOverlappingBookings(anyLong(), any(), any())).thenReturn(List.of());
        when(bookingRepository.save(any())).thenReturn(savedBooking);

        Booking booking = bookingService.createBooking(request, 1L);

        assertNotNull(booking);
        assertEquals(1L, booking.getId());
        verify(bookingRepository, times(1)).save(any());
    }

    @Test
    void approveBooking_ValidData_Success() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setRoomId(1L);
        booking.setStartDate(LocalDate.now().plusDays(1));
        booking.setEndDate(LocalDate.now().plusDays(3));
        booking.setStatus(BookingStatus.PENDING);

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));
        when(roomService.isRoomOwner(anyLong(), anyLong())).thenReturn(true);
        when(bookingRepository.save(any())).thenReturn(booking);

        Booking approvedBooking = bookingService.approveBooking(1L, 1L);

        assertEquals(BookingStatus.APPROVED, approvedBooking.getStatus());
        verify(bookingRepository, times(1)).save(any());
    }

    @Test
    void rejectBooking_ValidData_Success() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setRoomId(1L);
        booking.setStatus(BookingStatus.PENDING);

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));
        when(roomService.isRoomOwner(anyLong(), anyLong())).thenReturn(true);
        when(bookingRepository.save(any())).thenReturn(booking);

        Booking rejectedBooking = bookingService.rejectBooking(1L, 1L);

        assertEquals(BookingStatus.REJECTED, rejectedBooking.getStatus());
        verify(bookingRepository, times(1)).save(any());
    }

    @Test
    void cancelBooking_NotCreator_ThrowsAccessDeniedException() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setSeekerId(2L);

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        assertThrows(AccessDeniedException.class, () -> bookingService.cancelBooking(1L, 1L));
    }

    @Test
    void updateBooking_ValidData_Success() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setSeekerId(1L);
        booking.setStatus(BookingStatus.PENDING);
        booking.setStartDate(LocalDate.now().plusDays(1));
        booking.setEndDate(LocalDate.now().plusDays(3));

        BookingRequest request = new BookingRequest();
        request.setStartDate(LocalDate.now().plusDays(2));
        request.setEndDate(LocalDate.now().plusDays(4));

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenReturn(booking);

        Booking updatedBooking = bookingService.updateBooking(1L, request, 1L);

        assertEquals(request.getStartDate(), updatedBooking.getStartDate());
        assertEquals(request.getEndDate(), updatedBooking.getEndDate());
    }
}
