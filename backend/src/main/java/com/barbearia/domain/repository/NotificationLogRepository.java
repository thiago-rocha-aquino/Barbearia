package com.barbearia.domain.repository;

import com.barbearia.domain.entity.NotificationLog;
import com.barbearia.domain.enums.NotificationStatus;
import com.barbearia.domain.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLog, UUID> {

    List<NotificationLog> findByAppointmentId(UUID appointmentId);

    List<NotificationLog> findByStatus(NotificationStatus status);

    @Query("SELECT n FROM NotificationLog n WHERE n.appointment.id = :appointmentId AND n.type = :type")
    List<NotificationLog> findByAppointmentIdAndType(UUID appointmentId, NotificationType type);

    boolean existsByAppointmentIdAndTypeAndStatusIn(UUID appointmentId, NotificationType type,
                                                     List<NotificationStatus> statuses);
}
