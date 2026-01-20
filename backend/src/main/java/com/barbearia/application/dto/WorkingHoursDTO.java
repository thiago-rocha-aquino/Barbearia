package com.barbearia.application.dto;

import com.barbearia.domain.enums.DayOfWeekEnum;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public class WorkingHoursDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        @NotNull(message = "Dia da semana é obrigatório")
        private DayOfWeekEnum dayOfWeek;

        @NotNull(message = "Horário de início é obrigatório")
        private LocalTime startTime;

        @NotNull(message = "Horário de término é obrigatório")
        private LocalTime endTime;

        private boolean isWorking = true;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BulkRequest {
        @NotNull(message = "Lista de horários é obrigatória")
        @Size(min = 1, max = 7, message = "Deve conter entre 1 e 7 itens")
        private List<Request> workingHours;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private UUID id;
        private UUID barberId;
        private DayOfWeekEnum dayOfWeek;
        private LocalTime startTime;
        private LocalTime endTime;
        private boolean isWorking;
    }
}
