package com.roomfinder.repository;

import com.roomfinder.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByLandlordId(Long landlordId);
    List<Room> findByCityAndIsAvailableTrue(String city);

    @Query("SELECT r FROM Room r WHERE r.price <= :maxPrice AND r.isAvailable = true")
    List<Room> findAvailableRoomsByMaxPrice(double maxPrice);

    @Query("SELECT r FROM Room r WHERE r.address LIKE %:address% AND r.isAvailable = true ORDER BY " +
            "LENGTH(r.address) - LENGTH(REPLACE(r.address, :address, '')) DESC")
    List<Room> findRoomsBySimilarAddress(String address);

    Optional<Room> findByIdAndLandlordId(Long id, Long landlordId);
}
