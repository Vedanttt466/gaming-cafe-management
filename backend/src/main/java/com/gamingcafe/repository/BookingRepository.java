package com.gamingcafe.repository;

import com.gamingcafe.entity.Booking;
import com.gamingcafe.entity.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Page<Booking> findByCustomerIdOrderByBookingTimeDesc(Long customerId, Pageable pageable);

    List<Booking> findByStatusAndExpiresAtBefore(BookingStatus status, LocalDateTime now);

    @Query("select b from Booking b where b.status = :status and b.pc.id = :pcId " +
           "and b.bookingTime <= :windowEnd and b.expiresAt >= :windowStart")
    List<Booking> findOverlappingActiveBookingsForPc(@Param("pcId") Long pcId,
                                                       @Param("status") BookingStatus status,
                                                       @Param("windowStart") LocalDateTime windowStart,
                                                       @Param("windowEnd") LocalDateTime windowEnd);

    @Query("select count(b) from Booking b where b.status = :status " +
           "and b.bookingTime <= :windowEnd and b.expiresAt >= :windowStart")
    long countActiveBookingsInWindow(@Param("status") BookingStatus status,
                                      @Param("windowStart") LocalDateTime windowStart,
                                      @Param("windowEnd") LocalDateTime windowEnd);

    List<Booking> findByStatusIn(List<BookingStatus> statuses);

    long countByStatusAndCreatedAtBetween(BookingStatus status, LocalDateTime from, LocalDateTime to);
}
