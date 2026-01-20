package com.barbearia.domain.repository;

import com.barbearia.domain.entity.Appointment;
import com.barbearia.domain.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    Optional<Appointment> findByCancellationToken(String cancellationToken);

    @Query("SELECT a FROM Appointment a WHERE a.barber.id = :barberId " +
           "AND a.status IN ('SCHEDULED', 'CONFIRMED') " +
           "AND a.startTime < :endTime AND a.endTime > :startTime")
    List<Appointment> findOverlappingAppointments(UUID barberId, LocalDateTime startTime, LocalDateTime endTime);

    @Query("SELECT a FROM Appointment a WHERE a.barber.id = :barberId " +
           "AND a.status IN ('SCHEDULED', 'CONFIRMED') " +
           "AND a.startTime < :endTime AND a.endTime > :startTime " +
           "AND a.id != :excludeId")
    List<Appointment> findOverlappingAppointmentsExcluding(UUID barberId, LocalDateTime startTime,
                                                           LocalDateTime endTime, UUID excludeId);

    @Query("SELECT a FROM Appointment a JOIN FETCH a.service JOIN FETCH a.barber " +
           "WHERE a.barber.id = :barberId " +
           "AND a.startTime >= :startDate AND a.startTime < :endDate " +
           "ORDER BY a.startTime")
    List<Appointment> findByBarberIdAndDateRange(UUID barberId, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT a FROM Appointment a JOIN FETCH a.service JOIN FETCH a.barber " +
           "WHERE a.startTime >= :startDate AND a.startTime < :endDate " +
           "ORDER BY a.startTime")
    List<Appointment> findByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT a FROM Appointment a JOIN FETCH a.service JOIN FETCH a.barber " +
           "WHERE a.status IN ('SCHEDULED', 'CONFIRMED') " +
           "AND a.startTime >= :startDate AND a.startTime < :endDate " +
           "ORDER BY a.startTime")
    List<Appointment> findActiveByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT a FROM Appointment a JOIN FETCH a.service JOIN FETCH a.barber " +
           "WHERE a.barber.id = :barberId " +
           "AND a.status IN ('SCHEDULED', 'CONFIRMED') " +
           "AND a.startTime >= :now " +
           "ORDER BY a.startTime")
    List<Appointment> findUpcomingByBarberId(UUID barberId, LocalDateTime now);

    @Query("SELECT a FROM Appointment a " +
           "WHERE a.status IN ('SCHEDULED', 'CONFIRMED') " +
           "AND a.startTime BETWEEN :start AND :end")
    List<Appointment> findAppointmentsForReminder(LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.status = :status " +
           "AND a.startTime >= :startDate AND a.startTime < :endDate")
    long countByStatusAndDateRange(AppointmentStatus status, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT COALESCE(SUM(a.priceAtBooking), 0) FROM Appointment a " +
           "WHERE a.status = 'COMPLETED' " +
           "AND a.startTime >= :startDate AND a.startTime < :endDate")
    java.math.BigDecimal sumRevenueByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT a FROM Appointment a JOIN FETCH a.service JOIN FETCH a.barber " +
           "WHERE a.id = :id")
    Optional<Appointment> findByIdWithDetails(UUID id);

    List<Appointment> findByClientPhone(String clientPhone);
}
