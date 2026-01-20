package com.barbearia.application.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

public class TimeBlockDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        private UUID barberId;

        @NotNull(message = "Horário de início é obrigatório")
        private LocalDateTime startTime;

        @NotNull(message = "Horário de término é obrigatório")
        private LocalDateTime endTime;

        @NotBlank(message = "Motivo é obrigatório")
        @Size(max = 255, message = "Motivo deve ter no máximo 255 caracteres")
        private String reason;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private UUID id;
        private UUID barberId;
        private String barberName;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String reason;
    }
}
