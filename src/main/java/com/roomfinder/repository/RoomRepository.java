package com.roomfinder.repository;

import com.roomfinder.entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    Page<Room> findByLandlordId(Long landlordId, Pageable pageable);

    Page<Room> findByCityAndAvailableTrue(String city, Pageable pageable);

    @Query("SELECT r FROM Room r WHERE r.price <= :maxPrice AND r.available = true")
    Page<Room> findAvailableRoomsByMaxPrice(double maxPrice, Pageable pageable);

    @Query("SELECT r FROM Room r WHERE r.address LIKE %:address% AND r.available = true ORDER BY " +
            "LENGTH(r.address) - LENGTH(REPLACE(r.address, :address, '')) DESC")
    Page<Room> findRoomsBySimilarAddress(String address, Pageable pageable);

    Optional<Room> findByIdAndLandlordId(Long id, Long landlordId);

    @Query("SELECT r.id FROM Room r WHERE r.landlordId = :landlordId")
    List<Long> findRoomIdsByLandlordId(Long landlordId);

    @Query("SELECT r FROM Room r WHERE r.postedDate >= :date AND r.available = true")
    Page<Room> findNewListingsSince(LocalDateTime date, Pageable pageable);

    @Query("SELECT AVG(r.price) FROM Room r WHERE r.postedDate >= :date AND r.available = true")
    Double findAveragePriceSince(LocalDateTime date);

    @Query("SELECT r.city, AVG(r.price) as avgPrice, COUNT(r) as listingCount " +
            "FROM Room r WHERE r.postedDate >= :date AND r.available = true " +
            "GROUP BY r.city")
    List<Object[]> findAveragePriceByCitySince(LocalDateTime date);

    @Query("SELECT COUNT(r) FROM Room r")
    Long countAllRooms();

    @Query("SELECT COUNT(r) FROM Room r WHERE r.available = true")
    Long countAvailableRooms();

    @Query("SELECT COUNT(r) FROM Room r WHERE r.available = false")
    Long countOccupiedRooms();

    @Query("SELECT " +
            "CASE " +
            "WHEN r.price < 500 THEN '0-500' " +
            "WHEN r.price >= 500 AND r.price < 1000 THEN '500-1000' " +
            "WHEN r.price >= 1000 AND r.price < 1500 THEN '1000-1500' " +
            "WHEN r.price >= 1500 AND r.price < 2000 THEN '1500-2000' " +
            "ELSE '2000+' " +
            "END as priceRange, " +
            "COUNT(r) as count " +
            "FROM Room r " +
            "GROUP BY priceRange " +
            "ORDER BY priceRange")
    List<Object[]> countRoomsByPriceRange();

    @Query("SELECT r.city, COUNT(r) as count FROM Room r GROUP BY r.city ORDER BY count DESC")
    List<Object[]> countRoomsByCity();

}
