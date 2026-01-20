package com.barbearia.domain.entity;

import com.barbearia.domain.enums.AppointmentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "appointments", indexes = {
    @Index(name = "idx_appointment_barber_start", columnList = "barber_id, start_time"),
    @Index(name = "idx_appointment_status", columnList = "status"),
    @Index(name = "idx_appointment_client_phone", columnList = "client_phone")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barber_id", nullable = false)
    private User barber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;

    @Column(name = "client_name", nullable = false)
    private String clientName;

    @Column(name = "client_phone", nullable = false)
    private String clientPhone;

    @Column(name = "client_email")
    private String clientEmail;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AppointmentStatus status = AppointmentStatus.CONFIRMED;

    @Column(name = "price_at_booking", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceAtBooking;

    @Column(length = 500)
    private String notes;

    @Column(name = "cancellation_token", unique = true)
    private String cancellationToken;

    @Column(name = "created_by_admin", nullable = false)
    @Builder.Default
    private boolean createdByAdmin = false;

    @OneToMany(mappedBy = "appointment", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AppointmentAudit> audits = new ArrayList<>();

    @OneToMany(mappedBy = "appointment", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<NotificationLog> notifications = new ArrayList<>();

    @PrePersist
    public void generateCancellationToken() {
        if (this.cancellationToken == null) {
            this.cancellationToken = UUID.randomUUID().toString();
        }
    }

    public boolean canBeCancelledByClient(LocalDateTime now, int hoursLimit) {
        return startTime.minusHours(hoursLimit).isAfter(now);
    }

    public boolean isActive() {
        return status == AppointmentStatus.SCHEDULED || status == AppointmentStatus.CONFIRMED;
    }

    public boolean overlaps(LocalDateTime start, LocalDateTime end) {
        return startTime.isBefore(end) && endTime.isAfter(start);
    }
}
