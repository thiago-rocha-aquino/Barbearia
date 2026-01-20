package com.barbearia.infrastructure.notification;

import com.barbearia.domain.entity.Appointment;
import com.barbearia.domain.enums.NotificationType;

public interface NotificationProvider {

    String getChannel();

    boolean send(Appointment appointment, NotificationType type, String recipient, String content);

    String buildContent(Appointment appointment, NotificationType type);
}
