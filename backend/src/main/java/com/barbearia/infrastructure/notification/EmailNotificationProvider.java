package com.barbearia.infrastructure.notification;

import com.barbearia.domain.entity.Appointment;
import com.barbearia.domain.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationProvider implements NotificationProvider {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from:noreply@barbearia.com}")
    private String fromEmail;

    @Value("${app.business.name:Barbearia}")
    private String businessName;

    @Value("${app.business.address:}")
    private String businessAddress;

    @Value("${app.business.phone:}")
    private String businessPhone;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public String getChannel() {
        return "EMAIL";
    }

    @Override
    public boolean send(Appointment appointment, NotificationType type, String recipient, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(recipient);
            message.setSubject(getSubject(type));
            message.setText(content);

            mailSender.send(message);
            log.info("Email sent successfully to {} for appointment {}", recipient, appointment.getId());
            return true;
        } catch (Exception e) {
            log.error("Failed to send email to {} for appointment {}: {}",
                    recipient, appointment.getId(), e.getMessage());
            return false;
        }
    }

    @Override
    public String buildContent(Appointment appointment, NotificationType type) {
        String date = appointment.getStartTime().format(DATE_FORMATTER);
        String time = appointment.getStartTime().format(TIME_FORMATTER);
        String serviceName = appointment.getService().getName();
        String barberName = appointment.getBarber().getName();

        StringBuilder sb = new StringBuilder();
        sb.append("Olá ").append(appointment.getClientName()).append(",\n\n");

        switch (type) {
            case CONFIRMATION -> {
                sb.append("Seu agendamento foi confirmado!\n\n");
                sb.append("Detalhes:\n");
            }
            case REMINDER_24H -> {
                sb.append("Lembrete: seu agendamento é amanhã!\n\n");
                sb.append("Detalhes:\n");
            }
            case REMINDER_2H -> {
                sb.append("Lembrete: seu agendamento é em 2 horas!\n\n");
                sb.append("Detalhes:\n");
            }
            case CANCELLATION -> {
                sb.append("Seu agendamento foi cancelado.\n\n");
                sb.append("Detalhes do agendamento cancelado:\n");
            }
            case RESCHEDULE -> {
                sb.append("Seu agendamento foi reagendado!\n\n");
                sb.append("Novos detalhes:\n");
            }
        }

        sb.append("- Serviço: ").append(serviceName).append("\n");
        sb.append("- Data: ").append(date).append("\n");
        sb.append("- Horário: ").append(time).append("\n");
        sb.append("- Profissional: ").append(barberName).append("\n");

        if (!businessAddress.isEmpty()) {
            sb.append("- Local: ").append(businessAddress).append("\n");
        }

        sb.append("\n");

        if (type == NotificationType.CONFIRMATION || type == NotificationType.RESCHEDULE) {
            sb.append("Para cancelar ou reagendar, acesse o link enviado na confirmação.\n\n");
        }

        sb.append("Atenciosamente,\n");
        sb.append(businessName);

        if (!businessPhone.isEmpty()) {
            sb.append("\nTelefone: ").append(businessPhone);
        }

        return sb.toString();
    }

    private String getSubject(NotificationType type) {
        return switch (type) {
            case CONFIRMATION -> businessName + " - Agendamento Confirmado";
            case REMINDER_24H -> businessName + " - Lembrete: Agendamento Amanhã";
            case REMINDER_2H -> businessName + " - Lembrete: Agendamento em 2 horas";
            case CANCELLATION -> businessName + " - Agendamento Cancelado";
            case RESCHEDULE -> businessName + " - Agendamento Reagendado";
        };
    }
}
