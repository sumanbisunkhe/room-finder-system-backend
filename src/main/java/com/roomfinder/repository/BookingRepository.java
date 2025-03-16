package com.roomfinder.repository;

import com.roomfinder.entity.Booking;
import com.roomfinder.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findBySeekerId(Long seekerId);
    List<Booking> findByRoomId(Long roomId);
    List<Booking> findByRoomIdAndStatus(Long roomId, BookingStatus status);

    @Query("SELECT b FROM Booking b WHERE b.roomId = :roomId " +
            "AND b.status = 'APPROVED' " +
            "AND ((b.startDate BETWEEN :startDate AND :endDate) " +
            "OR (b.endDate BETWEEN :startDate AND :endDate))")
    List<Booking> findOverlappingBookings(Long roomId, LocalDate startDate, LocalDate endDate);
    List<Booking> findByRoomIdIn(List<Long> roomIds);

}