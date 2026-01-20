package com.barbearia.api.controller;

import com.barbearia.application.dto.ReportDTO;
import com.barbearia.application.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'BARBER')")
@Tag(name = "Relatórios", description = "Relatórios e estatísticas")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/dashboard")
    @Operation(summary = "Dashboard", description = "Retorna estatísticas do dashboard")
    public ResponseEntity<ReportDTO.DashboardStats> getDashboard() {
        return ResponseEntity.ok(reportService.getDashboardStats());
    }

    @GetMapping("/period")
    @Operation(summary = "Relatório por período", description = "Retorna relatório de agendamentos por período")
    public ResponseEntity<ReportDTO.PeriodReport> getPeriodReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(reportService.getPeriodReport(startDate, endDate));
    }
}
