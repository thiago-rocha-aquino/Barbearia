package com.barbearia.domain.repository;

import com.barbearia.domain.entity.AppointmentAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AppointmentAuditRepository extends JpaRepository<AppointmentAudit, UUID> {

    List<AppointmentAudit> findByAppointmentIdOrderByPerformedAtDesc(UUID appointmentId);
}
