package com.barbearia.application.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public class AvailabilityDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SlotRequest {
        private UUID serviceId;
        private UUID barberId;
        private LocalDate date;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TimeSlot {
        private LocalDateTime dateTime;
        private LocalTime time;
        private boolean available;
        private UUID barberId;
        private String barberName;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DayAvailability {
        private LocalDate date;
        private boolean hasAvailableSlots;
        private List<TimeSlot> slots;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MonthAvailability {
        private int year;
        private int month;
        private List<DayAvailability> days;
    }
}
