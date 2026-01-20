package com.barbearia.application.mapper;

import com.barbearia.application.dto.AppointmentDTO;
import com.barbearia.domain.entity.Appointment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AppointmentMapper {

    @Value("${app.booking.client-cancel-hours:4}")
    private int clientCancelHours;

    public AppointmentDTO.Response toResponse(Appointment appointment) {
        return AppointmentDTO.Response.builder()
                .id(appointment.getId())
                .barberId(appointment.getBarber().getId())
                .barberName(appointment.getBarber().getName())
                .serviceId(appointment.getService().getId())
                .serviceName(appointment.getService().getName())
                .serviceDuration(appointment.getService().getDurationMinutes())
                .clientName(appointment.getClientName())
                .clientPhone(appointment.getClientPhone())
                .clientEmail(appointment.getClientEmail())
                .startTime(appointment.getStartTime())
                .endTime(appointment.getEndTime())
                .status(appointment.getStatus())
                .priceAtBooking(appointment.getPriceAtBooking())
                .notes(appointment.getNotes())
                .cancellationToken(appointment.getCancellationToken())
                .createdByAdmin(appointment.isCreatedByAdmin())
                .createdAt(appointment.getCreatedAt())
                .build();
    }

    public AppointmentDTO.PublicResponse toPublicResponse(Appointment appointment) {
        LocalDateTime now = LocalDateTime.now();
        boolean canModify = appointment.isActive() && appointment.canBeCancelledByClient(now, clientCancelHours);

        return AppointmentDTO.PublicResponse.builder()
                .id(appointment.getId())
                .barberName(appointment.getBarber().getName())
                .serviceName(appointment.getService().getName())
                .serviceDuration(appointment.getService().getDurationMinutes())
                .startTime(appointment.getStartTime())
                .endTime(appointment.getEndTime())
                .price(appointment.getPriceAtBooking())
                .cancellationToken(appointment.getCancellationToken())
                .canCancel(canModify)
                .canReschedule(canModify)
                .build();
    }

    public AppointmentDTO.CalendarEvent toCalendarEvent(Appointment appointment) {
        return AppointmentDTO.CalendarEvent.builder()
                .id(appointment.getId())
                .title(appointment.getService().getName() + " - " + appointment.getClientName())
                .start(appointment.getStartTime())
                .end(appointment.getEndTime())
                .status(appointment.getStatus().name())
                .clientName(appointment.getClientName())
                .clientPhone(appointment.getClientPhone())
                .serviceName(appointment.getService().getName())
                .barberName(appointment.getBarber().getName())
                .barberId(appointment.getBarber().getId())
                .build();
    }
}
