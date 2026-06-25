package com.gamingcafe.repository;

import com.gamingcafe.entity.Pc;
import com.gamingcafe.entity.PcStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import jakarta.persistence.LockModeType;
import java.util.List;

public interface PcRepository extends JpaRepository<Pc, Long> {
    List<Pc> findByStatus(PcStatus status);
    List<Pc> findAllByOrderByPcNumberAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Pc p where p.id = :id")
    Pc lockById(Long id);
}
