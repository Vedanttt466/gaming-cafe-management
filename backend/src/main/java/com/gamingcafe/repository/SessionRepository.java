package com.gamingcafe.repository;

import com.gamingcafe.entity.Session;
import com.gamingcafe.entity.SessionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, Long> {

    List<Session> findByStatus(SessionStatus status);

    Optional<Session> findByPcIdAndStatus(Long pcId, SessionStatus status);

    Page<Session> findByCustomerIdOrderByStartTimeDesc(Long customerId, Pageable pageable);

    @Query("select coalesce(sum(s.netAmountDue + s.tokenAdjusted), 0) from Session s " +
           "where s.status = 'COMPLETED' and s.endTime between :from and :to")
    BigDecimal sumGrossRevenueBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    long countByStatusAndStartTimeBetween(SessionStatus status, LocalDateTime from, LocalDateTime to);

    @Query("select s from Session s where s.status = 'COMPLETED' and s.endTime between :from and :to")
    List<Session> findCompletedBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    long countByCustomerId(Long customerId);
}
