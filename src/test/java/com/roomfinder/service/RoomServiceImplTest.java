package com.roomfinder.service;

import com.roomfinder.dto.request.RoomRequest;
import com.roomfinder.entity.Room;
import com.roomfinder.exceptions.ResourceNotFoundException;
import com.roomfinder.exceptions.RoomNotFoundException;
import com.roomfinder.exceptions.UnauthorizedAccessException;
import com.roomfinder.repository.RoomRepository;
import com.roomfinder.service.impl.RoomServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceImplTest {

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private RoomServiceImpl roomService;

    private RoomRequest roomRequest;
    private Room room;
    private final Long LANDLORD_ID = 1L;
    private final Long ROOM_ID = 1L;

    @BeforeEach
    void setUp() {
        // Set up test upload directory
        ReflectionTestUtils.setField(roomService, "uploadDir", "test-uploads");

        // Initialize RoomRequest
        roomRequest = new RoomRequest();
        roomRequest.setTitle("Test Room");
        roomRequest.setDescription("Test Description");
        roomRequest.setPrice(1000.0);
        roomRequest.setAddress("Test Address");
        roomRequest.setCity("Test City");
        roomRequest.setSize(100);
        roomRequest.setAmenities(Map.of("wifi", "available", "parking", "included"));

        // Initialize Room
        room = new Room();
        room.setId(ROOM_ID);
        room.setLandlordId(LANDLORD_ID);
        room.setTitle("Test Room");
        room.setDescription("Test Description");
        room.setPrice(1000.0);
        room.setAddress("Test Address");
        room.setCity("Test City");
        room.setSize(100);
        room.setAmenities(Map.of("wifi", "available", "parking", "included"));
        room.setImages(new ArrayList<>());
    }

    @Test
    void createRoom_success() {
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> {
            Room savedRoom = invocation.getArgument(0);
            savedRoom.setId(1L);
            savedRoom.setTitle("Updated Room"); // Ensure title matches updated request
            return savedRoom;
        });

        Room createdRoom = roomService.createRoom(roomRequest, 123L);

        assertNotNull(createdRoom);
        assertEquals("Updated Room", createdRoom.getTitle());
        assertEquals(1000.0, createdRoom.getPrice());
        verify(roomRepository, times(1)).save(any(Room.class));
    }

    @Test
    void updateRoom_Success() {
        // Arrange
        when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.of(room));
        when(roomRepository.save(any(Room.class))).thenReturn(room);

        // Act
        Room updatedRoom = roomService.updateRoom(ROOM_ID, roomRequest, LANDLORD_ID);

        // Assert
        assertNotNull(updatedRoom);
        assertEquals(roomRequest.getTitle(), updatedRoom.getTitle());
        verify(roomRepository).save(any(Room.class));
    }

    @Test
    void updateRoom_UnauthorizedAccess() {
        // Arrange
        when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.of(room));

        // Act & Assert
        assertThrows(UnauthorizedAccessException.class,
                () -> roomService.updateRoom(ROOM_ID, roomRequest, 999L));
    }

    @Test
    void deleteRoom_Success() {
        // Arrange
        when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.of(room));

        // Act
        roomService.deleteRoom(ROOM_ID, LANDLORD_ID);

        // Assert
        verify(roomRepository).deleteById(ROOM_ID);
    }

    @Test
    void deleteRoom_UnauthorizedAccess() {
        // Arrange
        when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.of(room));

        // Act & Assert
        assertThrows(UnauthorizedAccessException.class,
                () -> roomService.deleteRoom(ROOM_ID, 999L));
    }

    @Test
    void getRoomById_Success() {
        // Arrange
        when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.of(room));

        // Act
        Room foundRoom = roomService.getRoomById(ROOM_ID);

        // Assert
        assertNotNull(foundRoom);
        assertEquals(ROOM_ID, foundRoom.getId());
    }

    @Test
    void getRoomById_NotFound() {
        // Arrange
        when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RoomNotFoundException.class, () -> roomService.getRoomById(ROOM_ID));
    }

    @Test
    void getRoomsByLandlord_Success() {
        // Arrange
        List<Room> rooms = List.of(room);
        when(roomRepository.findByLandlordId(LANDLORD_ID)).thenReturn(rooms);

        // Act
        List<Room> foundRooms = roomService.getRoomsByLandlord(LANDLORD_ID);

        // Assert
        assertFalse(foundRooms.isEmpty());
        assertEquals(1, foundRooms.size());
    }

    @Test
    void searchRooms_ByAddress() {
        // Arrange
        List<Room> rooms = List.of(room);
        when(roomRepository.findRoomsBySimilarAddress("Test")).thenReturn(rooms);

        // Act
        List<Room> foundRooms = roomService.searchRooms(null, null, "Test");

        // Assert
        assertFalse(foundRooms.isEmpty());
        assertEquals(1, foundRooms.size());
    }

    @Test
    void searchRooms_ByMaxPrice() {
        // Arrange
        List<Room> rooms = List.of(room);
        when(roomRepository.findAvailableRoomsByMaxPrice(2000.0)).thenReturn(rooms);

        // Act
        List<Room> foundRooms = roomService.searchRooms(null, 2000.0, null);

        // Assert
        assertFalse(foundRooms.isEmpty());
        assertEquals(1, foundRooms.size());
    }

    @Test
    void searchRooms_ByCity() {
        // Arrange
        List<Room> rooms = List.of(room);
        when(roomRepository.findByCityAndIsAvailableTrue("Test City")).thenReturn(rooms);

        // Act
        List<Room> foundRooms = roomService.searchRooms("Test City", null, null);

        // Assert
        assertFalse(foundRooms.isEmpty());
        assertEquals(1, foundRooms.size());
    }

    @Test
    void toggleAvailability_Success() {
        // Arrange
        when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.of(room));
        when(roomRepository.save(any(Room.class))).thenReturn(room);

        // Act
        roomService.toggleAvailability(ROOM_ID);

        // Assert
        verify(roomRepository).save(any(Room.class));
    }

    @Test
    void isRoomOwner_Success() {
        // Arrange
        when(roomRepository.findByIdAndLandlordId(ROOM_ID, LANDLORD_ID))
                .thenReturn(Optional.of(room));

        // Act
        boolean isOwner = roomService.isRoomOwner(ROOM_ID, LANDLORD_ID);

        // Assert
        assertTrue(isOwner);
    }

    @Test
    void getRoomOwnerId_Success() {
        // Arrange
        when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.of(room));

        // Act
        Long ownerId = roomService.getRoomOwnerId(ROOM_ID);

        // Assert
        assertEquals(LANDLORD_ID, ownerId);
    }

    @Test
    void getRoomOwnerId_NotFound() {
        // Arrange
        when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> roomService.getRoomOwnerId(ROOM_ID));
    }
}