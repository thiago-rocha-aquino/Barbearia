package com.barbearia.api.controller;

import com.barbearia.application.dto.AppointmentDTO;
import com.barbearia.application.dto.NotificationDTO;
import com.barbearia.application.service.AppointmentService;
import com.barbearia.infrastructure.notification.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/appointments")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'BARBER')")
@Tag(name = "Agendamentos (Admin)", description = "Gestão de agendamentos")
public class AdminAppointmentController {

    private final AppointmentService appointmentService;
    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Buscar agendamentos por período")
    public ResponseEntity<List<AppointmentDTO.CalendarEvent>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) UUID barberId) {
        return ResponseEntity.ok(appointmentService.getCalendarEvents(startDate, endDate, barberId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar agendamento por ID")
    public ResponseEntity<AppointmentDTO.Response> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(appointmentService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Criar agendamento manualmente")
    public ResponseEntity<AppointmentDTO.Response> create(
            @Valid @RequestBody AppointmentDTO.AdminCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(appointmentService.createAdmin(request, userDetails.getUsername()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar agendamento")
    public ResponseEntity<AppointmentDTO.Response> update(
            @PathVariable UUID id,
            @Valid @RequestBody AppointmentDTO.UpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(appointmentService.update(id, request, userDetails.getUsername()));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancelar agendamento")
    public ResponseEntity<AppointmentDTO.Response> cancel(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(appointmentService.cancelByAdmin(id, userDetails.getUsername()));
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "Marcar como concluído")
    public ResponseEntity<AppointmentDTO.Response> complete(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(appointmentService.markAsCompleted(id, userDetails.getUsername()));
    }

    @PostMapping("/{id}/no-show")
    @Operation(summary = "Marcar como no-show")
    public ResponseEntity<AppointmentDTO.Response> noShow(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(appointmentService.markAsNoShow(id, userDetails.getUsername()));
    }

    @GetMapping("/{id}/notifications")
    @Operation(summary = "Listar notificações do agendamento")
    public ResponseEntity<List<NotificationDTO.Response>> getNotifications(@PathVariable UUID id) {
        return ResponseEntity.ok(notificationService.findByAppointmentId(id));
    }

    @PostMapping("/notifications/{notificationId}/resend")
    @Operation(summary = "Reenviar notificação")
    public ResponseEntity<Void> resendNotification(@PathVariable UUID notificationId) {
        notificationService.resendNotification(notificationId);
        return ResponseEntity.ok().build();
    }
}
