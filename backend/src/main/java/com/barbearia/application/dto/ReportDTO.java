package com.barbearia.application.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ReportDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PeriodReport {
        private LocalDate startDate;
        private LocalDate endDate;
        private long totalAppointments;
        private long completedAppointments;
        private long cancelledAppointments;
        private long noShowAppointments;
        private BigDecimal estimatedRevenue;
        private BigDecimal actualRevenue;
        private double noShowRate;
        private double cancellationRate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DashboardStats {
        private long todayAppointments;
        private long weekAppointments;
        private long pendingAppointments;
        private BigDecimal weekRevenue;
        private long noShowsThisWeek;
    }
}
