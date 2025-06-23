package com.roomfinder.service;

import com.roomfinder.dto.request.RoomRequest;
import com.roomfinder.entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface RoomService {
    Room createRoom(RoomRequest request, Long landlordId);

    Room updateRoom(Long roomId, RoomRequest request, List<String> existingImages, Long landlordId) throws IOException;

    public void deleteRoom(Long roomId, Long landlordId);

    Room getRoomById(Long roomId);

    Page<Room> getRoomsByLandlord(Long landlordId, Pageable pageable);

    Page<Room> getAllRooms(Pageable pageable);

    Page<Room> searchRooms(String city, Double maxPrice, String address, Pageable pageable);

    void toggleAvailability(Long roomId, Long landlordId);

    void setAvailability(Long roomId, Long landlordId, boolean available);

    String saveImage(MultipartFile file);

    boolean isRoomOwner(Long roomId, Long landlordId);

    Long getRoomOwnerId(Long roomId);

    List<Long> getRoomIdsByLandlordId(Long landlordId);

    Page<Room> getNewListingsLast7Days(Pageable pageable);

    Double getAveragePriceLast7Days();

    Map<String, Object> getNewListingsStatsLast7Days();

    Map<String, Object> getPropertyStatusStats();

    Map<String, Long> getPriceRangeDistribution();

    Map<String, Long> getCityDistribution();


}