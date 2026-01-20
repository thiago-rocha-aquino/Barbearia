package com.barbearia.application.dto;

import com.barbearia.domain.enums.AppointmentStatus;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class AppointmentDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PublicCreateRequest {
        @NotNull(message = "Serviço é obrigatório")
        private UUID serviceId;

        private UUID barberId;

        @NotNull(message = "Horário é obrigatório")
        private LocalDateTime startTime;

        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
        private String clientName;

        @NotBlank(message = "Telefone é obrigatório")
        @Size(min = 14, max = 15, message = "Telefone deve ter entre 10 e 11 dígitos")
        @Pattern(regexp = "^\\(\\d{2}\\) \\d{4,5}-\\d{4}$", message = "Telefone inválido. Use formato: (11) 99999-9999")
        private String clientPhone;

        @Email(message = "Email inválido")
        private String clientEmail;

        @Size(max = 500, message = "Observações devem ter no máximo 500 caracteres")
        private String notes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AdminCreateRequest {
        @NotNull(message = "Serviço é obrigatório")
        private UUID serviceId;

        @NotNull(message = "Barbeiro é obrigatório")
        private UUID barberId;

        @NotNull(message = "Horário é obrigatório")
        private LocalDateTime startTime;

        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
        private String clientName;

        @NotBlank(message = "Telefone é obrigatório")
        private String clientPhone;

        @Email(message = "Email inválido")
        private String clientEmail;

        @Size(max = 500, message = "Observações devem ter no máximo 500 caracteres")
        private String notes;

        private AppointmentStatus status;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateRequest {
        private LocalDateTime startTime;
        private UUID barberId;
        private AppointmentStatus status;

        @Size(max = 500, message = "Observações devem ter no máximo 500 caracteres")
        private String notes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RescheduleRequest {
        @NotNull(message = "Novo horário é obrigatório")
        private LocalDateTime newStartTime;

        private UUID newBarberId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private UUID id;
        private UUID barberId;
        private String barberName;
        private UUID serviceId;
        private String serviceName;
        private Integer serviceDuration;
        private String clientName;
        private String clientPhone;
        private String clientEmail;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private AppointmentStatus status;
        private BigDecimal priceAtBooking;
        private String notes;
        private String cancellationToken;
        private boolean createdByAdmin;
        private LocalDateTime createdAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PublicResponse {
        private UUID id;
        private String barberName;
        private String serviceName;
        private Integer serviceDuration;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private BigDecimal price;
        private String cancellationToken;
        private boolean canCancel;
        private boolean canReschedule;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CalendarEvent {
        private UUID id;
        private String title;
        private LocalDateTime start;
        private LocalDateTime end;
        private String status;
        private String clientName;
        private String clientPhone;
        private String serviceName;
        private String barberName;
        private UUID barberId;
    }
}
