package com.barbearia.application.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

public class ServiceDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
        private String name;

        @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
        private String description;

        @NotNull(message = "Duração é obrigatória")
        @Min(value = 15, message = "Duração mínima é 15 minutos")
        @Max(value = 480, message = "Duração máxima é 480 minutos")
        private Integer durationMinutes;

        @Min(value = 0, message = "Buffer não pode ser negativo")
        @Max(value = 60, message = "Buffer máximo é 60 minutos")
        private Integer bufferMinutes = 0;

        @NotNull(message = "Preço é obrigatório")
        @DecimalMin(value = "0.00", message = "Preço deve ser positivo")
        private BigDecimal price;

        private Integer displayOrder;

        private Boolean active;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private UUID id;
        private String name;
        private String description;
        private Integer durationMinutes;
        private Integer bufferMinutes;
        private Integer totalDurationMinutes;
        private BigDecimal price;
        private Integer displayOrder;
        private boolean active;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PublicResponse {
        private UUID id;
        private String name;
        private String description;
        private Integer durationMinutes;
        private BigDecimal price;
    }
}
