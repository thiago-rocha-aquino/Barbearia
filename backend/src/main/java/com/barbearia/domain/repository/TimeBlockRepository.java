package com.barbearia.domain.repository;

import com.barbearia.domain.entity.TimeBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TimeBlockRepository extends JpaRepository<TimeBlock, UUID> {

    List<TimeBlock> findByBarberId(UUID barberId);

    @Query("SELECT tb FROM TimeBlock tb WHERE tb.barber.id = :barberId " +
           "AND tb.startTime < :endTime AND tb.endTime > :startTime")
    List<TimeBlock> findOverlappingBlocks(UUID barberId, LocalDateTime startTime, LocalDateTime endTime);

    @Query("SELECT tb FROM TimeBlock tb WHERE tb.barber.id = :barberId " +
           "AND tb.startTime >= :startDate AND tb.startTime < :endDate")
    List<TimeBlock> findByBarberIdAndDateRange(UUID barberId, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT tb FROM TimeBlock tb WHERE tb.startTime >= :startDate AND tb.startTime < :endDate")
    List<TimeBlock> findByDateRange(LocalDateTime startDate, LocalDateTime endDate);
}
