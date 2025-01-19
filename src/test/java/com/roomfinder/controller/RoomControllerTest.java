package com.roomfinder.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roomfinder.dto.request.RoomRequest;
import com.roomfinder.entity.Room;
import com.roomfinder.exceptions.GlobalExceptionHandler;
import com.roomfinder.exceptions.RoomNotFoundException;
import com.roomfinder.exceptions.UnauthorizedAccessException;
import com.roomfinder.service.RoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class RoomControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RoomService roomService;

    @InjectMocks
    private RoomController roomController;

    private ObjectMapper objectMapper;
    private Room testRoom;
    private RoomRequest testRoomRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(roomController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();

        // Initialize test room
        testRoom = new Room();
        testRoom.setId(1L);
        testRoom.setLandlordId(1L);
        testRoom.setTitle("Test Room");
        testRoom.setDescription("Test Description");
        testRoom.setPrice(1000.0);
        testRoom.setAddress("123 Test St");
        testRoom.setCity("Test City");
        testRoom.setSize(100);
        testRoom.setAvailable(true);

        // Initialize test room request
        testRoomRequest = new RoomRequest();
        testRoomRequest.setTitle("Test Room");
        testRoomRequest.setDescription("Test Description");
        testRoomRequest.setPrice(1000.0);
        testRoomRequest.setAddress("123 Test St");
        testRoomRequest.setCity("Test City");
        testRoomRequest.setSize(100);

        Map<String, String> amenities = new HashMap<>();
        amenities.put("wifi", "yes");
        testRoomRequest.setAmenities(amenities);
    }

    @Test
    void createRoom_Success() throws Exception {
        MockMultipartFile imageFile = new MockMultipartFile(
                "images",
                "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        when(roomService.createRoom(any(RoomRequest.class), eq(1L)))
                .thenReturn(testRoom);

        mockMvc.perform(multipart("/api/rooms")
                        .file(imageFile)
                        .param("title", testRoomRequest.getTitle())
                        .param("description", testRoomRequest.getDescription())
                        .param("price", String.valueOf(testRoomRequest.getPrice()))
                        .param("address", testRoomRequest.getAddress())
                        .param("city", testRoomRequest.getCity())
                        .param("size", String.valueOf(testRoomRequest.getSize()))
                        .header("X-Landlord-Id", "1")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Test Room")));

        verify(roomService).createRoom(any(RoomRequest.class), eq(1L));
    }

    @Test
    void updateRoom_Success() throws Exception {
        MockMultipartFile imageFile = new MockMultipartFile(
                "images",
                "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        when(roomService.updateRoom(eq(1L), any(RoomRequest.class), eq(1L)))
                .thenReturn(testRoom);

        mockMvc.perform(multipart("/api/rooms/{id}", 1L)
                        .file(imageFile)
                        .param("title", testRoomRequest.getTitle())
                        .param("description", testRoomRequest.getDescription())
                        .param("price", String.valueOf(testRoomRequest.getPrice()))
                        .param("address", testRoomRequest.getAddress())
                        .param("city", testRoomRequest.getCity())
                        .param("size", String.valueOf(testRoomRequest.getSize()))
                        .header("X-Landlord-Id", "1")
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Test Room")));

        verify(roomService).updateRoom(eq(1L), any(RoomRequest.class), eq(1L));
    }

    @Test
    void updateRoom_UnauthorizedAccess() throws Exception {
        MockMultipartFile imageFile = new MockMultipartFile(
                "images",
                "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        when(roomService.updateRoom(eq(1L), any(RoomRequest.class), eq(2L)))
                .thenThrow(new UnauthorizedAccessException("You are not authorized to update this room."));

        mockMvc.perform(multipart("/api/rooms/{id}", 1L)
                        .file(imageFile)
                        .param("title", testRoomRequest.getTitle())
                        .param("description", testRoomRequest.getDescription())
                        .param("price", String.valueOf(testRoomRequest.getPrice()))
                        .param("address", testRoomRequest.getAddress())
                        .param("city", testRoomRequest.getCity())
                        .param("size", String.valueOf(testRoomRequest.getSize()))
                        .header("X-Landlord-Id", "2")
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isForbidden()) // This will now work correctly
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("not authorized")));
    }

    @Test
    void deleteRoom_Success() throws Exception {
        doNothing().when(roomService).deleteRoom(1L, 1L);

        mockMvc.perform(delete("/api/rooms/{id}", 1L)
                        .header("X-Landlord-Id", "1"))
                .andExpect(status().isNoContent());

        verify(roomService).deleteRoom(1L, 1L);
    }

    @Test
    void getRoom_Success() throws Exception {
        when(roomService.getRoomById(1L)).thenReturn(testRoom);

        mockMvc.perform(get("/api/rooms/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Test Room")));

        verify(roomService).getRoomById(1L);
    }

    @Test
    void getRoom_NotFound() throws Exception {
        when(roomService.getRoomById(999L))
                .thenThrow(new RoomNotFoundException("Room not found with id: 999"));

        mockMvc.perform(get("/api/rooms/{id}", 999L))
                .andExpect(status().isNotFound()) // This will now work correctly
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("Room not found")));
    }

    @Test
    void getRoomsByLandlord_Success() throws Exception {
        List<Room> rooms = Arrays.asList(testRoom);
        when(roomService.getRoomsByLandlord(1L)).thenReturn(rooms);

        mockMvc.perform(get("/api/rooms/landlord/{landlordId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].title", is("Test Room")));

        verify(roomService).getRoomsByLandlord(1L);
    }

    @Test
    void searchRooms_Success() throws Exception {
        List<Room> rooms = Arrays.asList(testRoom);
        when(roomService.searchRooms("Test City", 1000.0, null))
                .thenReturn(rooms);

        mockMvc.perform(get("/api/rooms/search")
                        .param("city", "Test City")
                        .param("maxPrice", "1000.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].city", is("Test City")));

        verify(roomService).searchRooms("Test City", 1000.0, null);
    }

    @Test
    void toggleAvailability_Success() throws Exception {
        doNothing().when(roomService).toggleAvailability(1L);

        mockMvc.perform(patch("/api/rooms/{id}/availability", 1L))
                .andExpect(status().isOk());

        verify(roomService).toggleAvailability(1L);
    }
}