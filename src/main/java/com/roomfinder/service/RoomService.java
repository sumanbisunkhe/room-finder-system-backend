package com.roomfinder.service;

import com.roomfinder.dto.request.RoomRequest;
import com.roomfinder.entity.Room;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface RoomService {
    Room createRoom(RoomRequest request, Long landlordId);
    Room updateRoom(Long roomId, RoomRequest request, List<String> existingImages, Long landlordId) throws IOException;
    public void deleteRoom(Long roomId, Long landlordId);
    Room getRoomById(Long roomId);
    List<Room> getRoomsByLandlord(Long landlordId);
    List<Room> getAllRooms();
    List<Room> searchRooms(String city, Double maxPrice, String address);
    void toggleAvailability(Long roomId,Long landlordId);
    String saveImage(MultipartFile file);
    boolean isRoomOwner(Long roomId, Long landlordId);
    Long getRoomOwnerId(Long roomId);
    List<Long> getRoomIdsByLandlordId(Long landlordId);

}