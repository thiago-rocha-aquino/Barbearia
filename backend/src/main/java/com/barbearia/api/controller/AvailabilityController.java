package com.barbearia.api.controller;

import com.barbearia.application.dto.AvailabilityDTO;
import com.barbearia.application.service.AvailabilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/public/availability")
@RequiredArgsConstructor
@Tag(name = "Disponibilidade", description = "Consulta de horários disponíveis")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    @GetMapping("/slots")
    @Operation(summary = "Buscar horários disponíveis",
            description = "Retorna os slots disponíveis para uma data específica")
    public ResponseEntity<List<AvailabilityDTO.TimeSlot>> getAvailableSlots(
            @RequestParam UUID serviceId,
            @RequestParam(required = false) UUID barberId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(availabilityService.getAvailableSlots(serviceId, barberId, date));
    }

    @GetMapping("/month")
    @Operation(summary = "Buscar disponibilidade do mês",
            description = "Retorna quais dias têm horários disponíveis no mês")
    public ResponseEntity<List<AvailabilityDTO.DayAvailability>> getMonthAvailability(
            @RequestParam UUID serviceId,
            @RequestParam(required = false) UUID barberId,
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(availabilityService.getMonthAvailability(serviceId, barberId, year, month));
    }
}
