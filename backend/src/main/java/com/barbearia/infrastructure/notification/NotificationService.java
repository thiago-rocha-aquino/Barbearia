package com.barbearia.infrastructure.notification;

import com.barbearia.application.dto.NotificationDTO;
import com.barbearia.application.exception.ResourceNotFoundException;
import com.barbearia.domain.entity.Appointment;
import com.barbearia.domain.entity.NotificationLog;
import com.barbearia.domain.enums.NotificationStatus;
import com.barbearia.domain.enums.NotificationType;
import com.barbearia.domain.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationService {

    private final NotificationLogRepository notificationLogRepository;
    private final EmailNotificationProvider emailProvider;

    @Value("${app.notifications.enabled:true}")
    private boolean notificationsEnabled;

    public void sendNotification(Appointment appointment, NotificationType type) {
        if (!notificationsEnabled) {
            log.debug("Notifications disabled, skipping");
            return;
        }

        String recipient = appointment.getClientEmail();
        if (recipient == null || recipient.isBlank()) {
            log.debug("No email for appointment {}, skipping notification", appointment.getId());
            return;
        }

        String content = emailProvider.buildContent(appointment, type);

        NotificationLog notification = NotificationLog.builder()
                .appointment(appointment)
                .type(type)
                .channel(emailProvider.getChannel())
                .recipient(recipient)
                .content(content)
                .status(NotificationStatus.PENDING)
                .build();

        notification = notificationLogRepository.save(notification);

        boolean sent = emailProvider.send(appointment, type, recipient, content);

        if (sent) {
            notification.markAsSent();
        } else {
            notification.markAsFailed("Falha no envio de email");
        }

        notificationLogRepository.save(notification);
    }

    public void resendNotification(UUID notificationId) {
        log.info("Resending notification: {}", notificationId);

        NotificationLog notification = notificationLogRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notificação", "id", notificationId));

        Appointment appointment = notification.getAppointment();

        boolean sent = emailProvider.send(
                appointment,
                notification.getType(),
                notification.getRecipient(),
                notification.getContent()
        );

        if (sent) {
            notification.markAsSent();
        } else {
            notification.markAsFailed("Falha no reenvio de email");
        }

        notificationLogRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public List<NotificationDTO.Response> findByAppointmentId(UUID appointmentId) {
        return notificationLogRepository.findByAppointmentId(appointmentId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean hasNotificationSent(UUID appointmentId, NotificationType type) {
        return notificationLogRepository.existsByAppointmentIdAndTypeAndStatusIn(
                appointmentId,
                type,
                List.of(NotificationStatus.SENT, NotificationStatus.PENDING)
        );
    }

    private NotificationDTO.Response toResponse(NotificationLog log) {
        return NotificationDTO.Response.builder()
                .id(log.getId())
                .appointmentId(log.getAppointment().getId())
                .type(log.getType())
                .channel(log.getChannel())
                .recipient(log.getRecipient())
                .status(log.getStatus())
                .errorMessage(log.getErrorMessage())
                .sentAt(log.getSentAt())
                .createdAt(log.getCreatedAt())
                .retryCount(log.getRetryCount())
                .build();
    }
}
