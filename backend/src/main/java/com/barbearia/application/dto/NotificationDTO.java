package com.barbearia.application.dto;

import com.barbearia.domain.enums.NotificationStatus;
import com.barbearia.domain.enums.NotificationType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

public class NotificationDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private UUID id;
        private UUID appointmentId;
        private NotificationType type;
        private String channel;
        private String recipient;
        private NotificationStatus status;
        private String errorMessage;
        private LocalDateTime sentAt;
        private LocalDateTime createdAt;
        private int retryCount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ResendRequest {
        private UUID notificationId;
    }
}
