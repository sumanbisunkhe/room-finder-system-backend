package com.roomfinder.service;

import com.roomfinder.dto.request.RoomRequest;
import com.roomfinder.entity.Room;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface RoomService {
    Room createRoom(RoomRequest request, Long landlordId);
    Room updateRoom(Long roomId, RoomRequest request, Long landlordId);
    public void deleteRoom(Long roomId, Long landlordId);
    Room getRoomById(Long roomId);
    List<Room> getRoomsByLandlord(Long landlordId);
    List<Room> searchRooms(String city, Double maxPrice, String address);
    void toggleAvailability(Long roomId);
    String saveImage(MultipartFile file);
    boolean isRoomOwner(Long roomId, Long landlordId);
    Long getRoomOwnerId(Long roomId);

}