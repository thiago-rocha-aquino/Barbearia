package com.barbearia.api.controller;

import com.barbearia.application.dto.AppointmentDTO;
import com.barbearia.application.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/booking")
@RequiredArgsConstructor
@Tag(name = "Agendamento (Público)", description = "Agendamento público de horários")
public class BookingController {

    private final AppointmentService appointmentService;

    @PostMapping
    @Operation(summary = "Criar agendamento", description = "Cria um novo agendamento pelo cliente")
    public ResponseEntity<AppointmentDTO.Response> createBooking(
            @Valid @RequestBody AppointmentDTO.PublicCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(appointmentService.createPublic(request));
    }

    @GetMapping("/{token}")
    @Operation(summary = "Buscar agendamento por token",
            description = "Retorna os detalhes do agendamento pelo token de cancelamento")
    public ResponseEntity<AppointmentDTO.PublicResponse> getByToken(@PathVariable String token) {
        return ResponseEntity.ok(appointmentService.findByToken(token));
    }

    @PostMapping("/{token}/cancel")
    @Operation(summary = "Cancelar agendamento", description = "Cancela um agendamento pelo cliente")
    public ResponseEntity<AppointmentDTO.PublicResponse> cancelBooking(@PathVariable String token) {
        return ResponseEntity.ok(appointmentService.cancelByClient(token));
    }

    @PostMapping("/{token}/reschedule")
    @Operation(summary = "Reagendar", description = "Reagenda um agendamento pelo cliente")
    public ResponseEntity<AppointmentDTO.PublicResponse> rescheduleBooking(
            @PathVariable String token,
            @Valid @RequestBody AppointmentDTO.RescheduleRequest request) {
        return ResponseEntity.ok(appointmentService.rescheduleByClient(token, request));
    }
}
