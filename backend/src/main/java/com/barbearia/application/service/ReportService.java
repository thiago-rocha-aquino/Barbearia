package com.barbearia.application.service;

import com.barbearia.application.dto.ReportDTO;
import com.barbearia.domain.enums.AppointmentStatus;
import com.barbearia.domain.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReportService {

    private final AppointmentRepository appointmentRepository;

    public ReportDTO.PeriodReport getPeriodReport(LocalDate startDate, LocalDate endDate) {
        log.info("Generating period report from {} to {}", startDate, endDate);

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        long total = countAllInPeriod(start, end);
        long completed = appointmentRepository.countByStatusAndDateRange(
                AppointmentStatus.COMPLETED, start, end);
        long cancelledByClient = appointmentRepository.countByStatusAndDateRange(
                AppointmentStatus.CANCELLED_BY_CLIENT, start, end);
        long cancelledByAdmin = appointmentRepository.countByStatusAndDateRange(
                AppointmentStatus.CANCELLED_BY_ADMIN, start, end);
        long noShow = appointmentRepository.countByStatusAndDateRange(
                AppointmentStatus.NO_SHOW, start, end);

        BigDecimal actualRevenue = appointmentRepository.sumRevenueByDateRange(start, end);

        long scheduled = total - completed - cancelledByClient - cancelledByAdmin - noShow;
        BigDecimal estimatedRevenue = actualRevenue.multiply(
                BigDecimal.valueOf((double) total / Math.max(completed, 1)))
                .setScale(2, RoundingMode.HALF_UP);

        double noShowRate = total > 0 ? (double) noShow / total * 100 : 0;
        double cancellationRate = total > 0 ?
                (double) (cancelledByClient + cancelledByAdmin) / total * 100 : 0;

        return ReportDTO.PeriodReport.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalAppointments(total)
                .completedAppointments(completed)
                .cancelledAppointments(cancelledByClient + cancelledByAdmin)
                .noShowAppointments(noShow)
                .estimatedRevenue(estimatedRevenue)
                .actualRevenue(actualRevenue)
                .noShowRate(Math.round(noShowRate * 100.0) / 100.0)
                .cancellationRate(Math.round(cancellationRate * 100.0) / 100.0)
                .build();
    }

    public ReportDTO.DashboardStats getDashboardStats() {
        log.debug("Generating dashboard stats");

        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.plusDays(1).atStartOfDay();

        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1L);
        LocalDateTime weekStartTime = weekStart.atStartOfDay();
        LocalDateTime weekEndTime = weekStart.plusDays(7).atStartOfDay();

        long todayAppointments = countActiveInPeriod(todayStart, todayEnd);
        long weekAppointments = countActiveInPeriod(weekStartTime, weekEndTime);

        long pending = appointmentRepository.countByStatusAndDateRange(
                AppointmentStatus.SCHEDULED, todayStart, todayEnd) +
                appointmentRepository.countByStatusAndDateRange(
                        AppointmentStatus.CONFIRMED, todayStart, todayEnd);

        BigDecimal weekRevenue = appointmentRepository.sumRevenueByDateRange(weekStartTime, weekEndTime);

        long noShowsThisWeek = appointmentRepository.countByStatusAndDateRange(
                AppointmentStatus.NO_SHOW, weekStartTime, weekEndTime);

        return ReportDTO.DashboardStats.builder()
                .todayAppointments(todayAppointments)
                .weekAppointments(weekAppointments)
                .pendingAppointments(pending)
                .weekRevenue(weekRevenue)
                .noShowsThisWeek(noShowsThisWeek)
                .build();
    }

    private long countAllInPeriod(LocalDateTime start, LocalDateTime end) {
        long count = 0;
        for (AppointmentStatus status : AppointmentStatus.values()) {
            count += appointmentRepository.countByStatusAndDateRange(status, start, end);
        }
        return count;
    }

    private long countActiveInPeriod(LocalDateTime start, LocalDateTime end) {
        return appointmentRepository.countByStatusAndDateRange(AppointmentStatus.SCHEDULED, start, end) +
               appointmentRepository.countByStatusAndDateRange(AppointmentStatus.CONFIRMED, start, end);
    }
}
