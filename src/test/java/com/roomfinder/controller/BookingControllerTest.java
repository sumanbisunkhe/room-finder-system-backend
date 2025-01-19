package com.roomfinder.controller;

import com.roomfinder.dto.request.BookingRequest;
import com.roomfinder.entity.Booking;
import com.roomfinder.enums.BookingStatus;
import com.roomfinder.security.CustomUserDetails;
import com.roomfinder.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingControllerTest {

    @Mock
    private BookingService bookingService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private BookingController bookingController;

    private CustomUserDetails userDetails;
    private Booking testBooking;
    private BookingRequest bookingRequest;
    private final Long TEST_USER_ID = 1L;
    private final Long TEST_BOOKING_ID = 1L;
    private final Long TEST_ROOM_ID = 1L;

    @BeforeEach
    void setUp() {
        // Setup CustomUserDetails
        userDetails = new CustomUserDetails(
                "test@example.com",
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")),
                TEST_USER_ID
        );

        // Setup test booking
        testBooking = new Booking();
        testBooking.setId(TEST_BOOKING_ID);
        testBooking.setRoomId(TEST_ROOM_ID);
        testBooking.setSeekerId(TEST_USER_ID);
        testBooking.setStartDate(LocalDate.now().plusDays(1));
        testBooking.setEndDate(LocalDate.now().plusDays(3));
        testBooking.setStatus(BookingStatus.PENDING);
        testBooking.setComments("Test booking");

        // Setup booking request
        bookingRequest = new BookingRequest();
        bookingRequest.setRoomId(TEST_ROOM_ID);
        bookingRequest.setStartDate(LocalDate.now().plusDays(1));
        bookingRequest.setEndDate(LocalDate.now().plusDays(3));
        bookingRequest.setComments("Test booking");
    }

    @Test
    void createBooking_Success() {
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(bookingService.createBooking(any(BookingRequest.class), eq(TEST_USER_ID)))
                .thenReturn(testBooking);

        ResponseEntity<Booking> response = bookingController.createBooking(bookingRequest, authentication);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(TEST_BOOKING_ID, response.getBody().getId());
        verify(bookingService).createBooking(any(BookingRequest.class), eq(TEST_USER_ID));
    }

    @Test
    void updateBooking_Success() {
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(bookingService.updateBooking(eq(TEST_BOOKING_ID), any(BookingRequest.class), eq(TEST_USER_ID)))
                .thenReturn(testBooking);

        ResponseEntity<Booking> response = bookingController.updateBooking(TEST_BOOKING_ID, bookingRequest, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(TEST_BOOKING_ID, response.getBody().getId());
        verify(bookingService).updateBooking(eq(TEST_BOOKING_ID), any(BookingRequest.class), eq(TEST_USER_ID));
    }

    @Test
    void approveBooking_Success() {
        when(authentication.getPrincipal()).thenReturn(userDetails);
        testBooking.setStatus(BookingStatus.APPROVED);
        when(bookingService.approveBooking(TEST_BOOKING_ID, TEST_USER_ID)).thenReturn(testBooking);

        ResponseEntity<Booking> response = bookingController.approveBooking(TEST_BOOKING_ID, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(BookingStatus.APPROVED, response.getBody().getStatus());
        verify(bookingService).approveBooking(TEST_BOOKING_ID, TEST_USER_ID);
    }

    @Test
    void rejectBooking_Success() {
        when(authentication.getPrincipal()).thenReturn(userDetails);
        testBooking.setStatus(BookingStatus.REJECTED);
        when(bookingService.rejectBooking(TEST_BOOKING_ID, TEST_USER_ID)).thenReturn(testBooking);

        ResponseEntity<Booking> response = bookingController.rejectBooking(TEST_BOOKING_ID, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(BookingStatus.REJECTED, response.getBody().getStatus());
        verify(bookingService).rejectBooking(TEST_BOOKING_ID, TEST_USER_ID);
    }

    @Test
    void cancelBooking_Success() {
        when(authentication.getPrincipal()).thenReturn(userDetails);
        testBooking.setStatus(BookingStatus.CANCELLED);
        when(bookingService.cancelBooking(TEST_BOOKING_ID, TEST_USER_ID)).thenReturn(testBooking);

        ResponseEntity<Booking> response = bookingController.cancelBooking(TEST_BOOKING_ID, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(BookingStatus.CANCELLED, response.getBody().getStatus());
        verify(bookingService).cancelBooking(TEST_BOOKING_ID, TEST_USER_ID);
    }

    @Test
    void getBooking_Success() {
        when(bookingService.getBookingById(TEST_BOOKING_ID)).thenReturn(testBooking);

        ResponseEntity<Booking> response = bookingController.getBooking(TEST_BOOKING_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(TEST_BOOKING_ID, response.getBody().getId());
        verify(bookingService).getBookingById(TEST_BOOKING_ID);
    }

    @Test
    void getBookingsBySeeker_Success() {
        List<Booking> bookings = Arrays.asList(testBooking);
        when(bookingService.getBookingsBySeeker(TEST_USER_ID)).thenReturn(bookings);

        ResponseEntity<List<Booking>> response = bookingController.getBookingsBySeeker(TEST_USER_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(TEST_BOOKING_ID, response.getBody().get(0).getId());
        verify(bookingService).getBookingsBySeeker(TEST_USER_ID);
    }

    @Test
    void getBookingsByRoom_Success() {
        List<Booking> bookings = Arrays.asList(testBooking);
        when(bookingService.getBookingsByRoom(TEST_ROOM_ID)).thenReturn(bookings);

        ResponseEntity<List<Booking>> response = bookingController.getBookingsByRoom(TEST_ROOM_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(TEST_ROOM_ID, response.getBody().get(0).getRoomId());
        verify(bookingService).getBookingsByRoom(TEST_ROOM_ID);
    }

    @Test
    void getPendingBookingsByRoom_Success() {
        List<Booking> bookings = Arrays.asList(testBooking);
        when(bookingService.getPendingBookingsByRoom(TEST_ROOM_ID)).thenReturn(bookings);

        ResponseEntity<List<Booking>> response = bookingController.getPendingBookingsByRoom(TEST_ROOM_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(BookingStatus.PENDING, response.getBody().get(0).getStatus());
        verify(bookingService).getPendingBookingsByRoom(TEST_ROOM_ID);
    }
}