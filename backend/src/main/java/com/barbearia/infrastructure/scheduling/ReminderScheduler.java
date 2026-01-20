package com.barbearia.infrastructure.scheduling;

import com.barbearia.domain.entity.Appointment;
import com.barbearia.domain.enums.NotificationType;
import com.barbearia.domain.repository.AppointmentRepository;
import com.barbearia.infrastructure.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReminderScheduler {

    private final AppointmentRepository appointmentRepository;
    private final NotificationService notificationService;

    @Value("${app.notifications.reminder-24h:true}")
    private boolean reminder24hEnabled;

    @Value("${app.notifications.reminder-2h:true}")
    private boolean reminder2hEnabled;

    @Scheduled(fixedRate = 900000) // 15 minutes
    @Transactional
    public void sendReminders() {
        log.debug("Running reminder scheduler");

        if (reminder24hEnabled) {
            send24HourReminders();
        }

        if (reminder2hEnabled) {
            send2HourReminders();
        }
    }

    private void send24HourReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.plusHours(23).plusMinutes(45);
        LocalDateTime end = now.plusHours(24).plusMinutes(15);

        List<Appointment> appointments = appointmentRepository.findAppointmentsForReminder(start, end);

        log.debug("Found {} appointments for 24h reminder", appointments.size());

        for (Appointment appointment : appointments) {
            if (!notificationService.hasNotificationSent(appointment.getId(), NotificationType.REMINDER_24H)) {
                try {
                    notificationService.sendNotification(appointment, NotificationType.REMINDER_24H);
                    log.info("Sent 24h reminder for appointment {}", appointment.getId());
                } catch (Exception e) {
                    log.error("Failed to send 24h reminder for appointment {}: {}",
                            appointment.getId(), e.getMessage());
                }
            }
        }
    }

    private void send2HourReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.plusHours(1).plusMinutes(45);
        LocalDateTime end = now.plusHours(2).plusMinutes(15);

        List<Appointment> appointments = appointmentRepository.findAppointmentsForReminder(start, end);

        log.debug("Found {} appointments for 2h reminder", appointments.size());

        for (Appointment appointment : appointments) {
            if (!notificationService.hasNotificationSent(appointment.getId(), NotificationType.REMINDER_2H)) {
                try {
                    notificationService.sendNotification(appointment, NotificationType.REMINDER_2H);
                    log.info("Sent 2h reminder for appointment {}", appointment.getId());
                } catch (Exception e) {
                    log.error("Failed to send 2h reminder for appointment {}: {}",
                            appointment.getId(), e.getMessage());
                }
            }
        }
    }
}
