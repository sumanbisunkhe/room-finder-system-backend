package com.roomfinder.repository;

import com.roomfinder.entity.Booking;
import com.roomfinder.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Page<Booking> findBySeekerIdOrderByIdDesc(Long seekerId, Pageable pageable);

    Page<Booking> findByRoomIdOrderByIdDesc(Long roomId, Pageable pageable);

    Page<Booking> findByRoomIdAndStatusOrderByIdDesc(Long roomId, BookingStatus status, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.roomId = :roomId " +
            "AND b.status = 'APPROVED' " +
            "AND ((b.startDate BETWEEN :startDate AND :endDate) " +
            "OR (b.endDate BETWEEN :startDate AND :endDate))")
    List<Booking> findOverlappingBookings(Long roomId, LocalDate startDate, LocalDate endDate);

    Page<Booking> findByRoomIdInOrderByIdDesc(List<Long> roomIds, Pageable pageable);

    Page<Booking> findByStatusOrderByIdDesc(BookingStatus status, Pageable pageable);

    Page<Booking> findBySeekerIdAndStatusOrderByIdDesc(Long seekerId, BookingStatus status, Pageable pageable);

    Page<Booking> findByRoomIdInAndStatusOrderByIdDesc(List<Long> roomIds, BookingStatus status, Pageable pageable);

    Page<Booking> findBySeekerIdAndRoomIdOrderByIdDesc(Long seekerId, Long roomId, Pageable pageable);

    Page<Booking> findBySeekerIdAndRoomIdAndStatusOrderByIdDesc(Long seekerId, Long roomId, BookingStatus status, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE " +
            "(:seekerId IS NULL OR b.seekerId = :seekerId) AND " +
            "(:roomId IS NULL OR b.roomId = :roomId) AND " +
            "(:status IS NULL OR b.status = :status) AND " +
            "(:startDateFrom IS NULL OR b.startDate >= :startDateFrom) AND " +
            "(:startDateTo IS NULL OR b.startDate <= :startDateTo) AND " +
            "(:endDateFrom IS NULL OR b.endDate >= :endDateFrom) AND " +
            "(:endDateTo IS NULL OR b.endDate <= :endDateTo) " +
            "ORDER BY b.id DESC")
    Page<Booking> searchBookings(
            Long seekerId,
            Long roomId,
            BookingStatus status,
            LocalDate startDateFrom,
            LocalDate startDateTo,
            LocalDate endDateFrom,
            LocalDate endDateTo,
            Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.roomId IN :roomIds AND " +
            "(:seekerId IS NULL OR b.seekerId = :seekerId) AND " +
            "(:status IS NULL OR b.status = :status) AND " +
            "(:startDateFrom IS NULL OR b.startDate >= :startDateFrom) AND " +
            "(:startDateTo IS NULL OR b.startDate <= :startDateTo) AND " +
            "(:endDateFrom IS NULL OR b.endDate >= :endDateFrom) AND " +
            "(:endDateTo IS NULL OR b.endDate <= :endDateTo) " +
            "ORDER BY b.id DESC")
    Page<Booking> searchBookingsForLandlord(
            List<Long> roomIds,
            Long seekerId,
            BookingStatus status,
            LocalDate startDateFrom,
            LocalDate startDateTo,
            LocalDate endDateFrom,
            LocalDate endDateTo,
            Pageable pageable);
}