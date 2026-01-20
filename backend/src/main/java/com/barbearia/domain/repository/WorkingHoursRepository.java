package com.barbearia.domain.repository;

import com.barbearia.domain.entity.WorkingHours;
import com.barbearia.domain.enums.DayOfWeekEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkingHoursRepository extends JpaRepository<WorkingHours, UUID> {

    List<WorkingHours> findByBarberId(UUID barberId);

    Optional<WorkingHours> findByBarberIdAndDayOfWeek(UUID barberId, DayOfWeekEnum dayOfWeek);

    void deleteByBarberId(UUID barberId);
}
