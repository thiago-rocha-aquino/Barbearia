package com.barbearia.api.controller;

import com.barbearia.application.dto.TimeBlockDTO;
import com.barbearia.application.service.TimeBlockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/time-blocks")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'BARBER')")
@Tag(name = "Bloqueios (Admin)", description = "Gestão de bloqueios de horário")
public class AdminTimeBlockController {

    private final TimeBlockService timeBlockService;

    @GetMapping
    @Operation(summary = "Listar bloqueios por período")
    public ResponseEntity<List<TimeBlockDTO.Response>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(timeBlockService.findByDateRange(startDate, endDate));
    }

    @GetMapping("/barber/{barberId}")
    @Operation(summary = "Listar bloqueios por barbeiro")
    public ResponseEntity<List<TimeBlockDTO.Response>> getByBarberId(@PathVariable UUID barberId) {
        return ResponseEntity.ok(timeBlockService.findByBarberId(barberId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar bloqueio por ID")
    public ResponseEntity<TimeBlockDTO.Response> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(timeBlockService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Criar bloqueio")
    public ResponseEntity<TimeBlockDTO.Response> create(@Valid @RequestBody TimeBlockDTO.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(timeBlockService.create(request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir bloqueio")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        timeBlockService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
