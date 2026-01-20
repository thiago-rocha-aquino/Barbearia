package com.barbearia.application.service;

import com.barbearia.application.dto.AppointmentDTO;
import com.barbearia.application.exception.BusinessException;
import com.barbearia.application.exception.ConflictException;
import com.barbearia.application.exception.ResourceNotFoundException;
import com.barbearia.application.mapper.AppointmentMapper;
import com.barbearia.domain.entity.*;
import com.barbearia.domain.enums.AppointmentStatus;
import com.barbearia.domain.enums.DayOfWeekEnum;
import com.barbearia.domain.enums.NotificationType;
import com.barbearia.domain.repository.*;
import com.barbearia.infrastructure.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;
    private final TimeBlockRepository timeBlockRepository;
    private final WorkingHoursRepository workingHoursRepository;
    private final AppointmentAuditRepository auditRepository;
    private final AppointmentMapper appointmentMapper;
    private final NotificationService notificationService;

    @Value("${app.booking.min-advance-hours:1}")
    private int minAdvanceHours;

    @Value("${app.booking.max-days-ahead:30}")
    private int maxDaysAhead;

    @Value("${app.booking.client-cancel-hours:4}")
    private int clientCancelHours;

    public AppointmentDTO.Response createPublic(AppointmentDTO.PublicCreateRequest request) {
        log.info("Creating public appointment for client: {}", request.getClientName());

        Service service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Serviço", "id", request.getServiceId()));

        if (!service.isActive()) {
            throw new BusinessException("SERVICE_INACTIVE", "Serviço não está ativo");
        }

        User barber;
        if (request.getBarberId() != null) {
            barber = userRepository.findById(request.getBarberId())
                    .orElseThrow(() -> new ResourceNotFoundException("Barbeiro", "id", request.getBarberId()));
        } else {
            List<User> barbers = userRepository.findAllActiveBarbers();
            if (barbers.isEmpty()) {
                throw new BusinessException("NO_BARBER", "Não há barbeiros disponíveis");
            }
            barber = findAvailableBarber(barbers, service, request.getStartTime());
        }

        validateBookingTime(request.getStartTime(), barber.getId(), service);

        LocalDateTime endTime = request.getStartTime().plusMinutes(service.getTotalDurationMinutes());

        validateNoConflicts(barber.getId(), request.getStartTime(), endTime, null);

        Appointment appointment = Appointment.builder()
                .barber(barber)
                .service(service)
                .clientName(request.getClientName())
                .clientPhone(request.getClientPhone())
                .clientEmail(request.getClientEmail())
                .startTime(request.getStartTime())
                .endTime(endTime)
                .status(AppointmentStatus.CONFIRMED)
                .priceAtBooking(service.getPrice())
                .notes(request.getNotes())
                .createdByAdmin(false)
                .build();

        appointment = appointmentRepository.save(appointment);

        createAudit(appointment, "CREATED", "client", null);

        notificationService.sendNotification(appointment, NotificationType.CONFIRMATION);

        log.info("Public appointment created with id: {}", appointment.getId());
        return appointmentMapper.toResponse(appointment);
    }

    public AppointmentDTO.Response createAdmin(AppointmentDTO.AdminCreateRequest request, String adminEmail) {
        log.info("Creating admin appointment by: {}", adminEmail);

        Service service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Serviço", "id", request.getServiceId()));

        User barber = userRepository.findById(request.getBarberId())
                .orElseThrow(() -> new ResourceNotFoundException("Barbeiro", "id", request.getBarberId()));

        LocalDateTime endTime = request.getStartTime().plusMinutes(service.getTotalDurationMinutes());

        validateNoConflicts(barber.getId(), request.getStartTime(), endTime, null);

        Appointment appointment = Appointment.builder()
                .barber(barber)
                .service(service)
                .clientName(request.getClientName())
                .clientPhone(request.getClientPhone())
                .clientEmail(request.getClientEmail())
                .startTime(request.getStartTime())
                .endTime(endTime)
                .status(request.getStatus() != null ? request.getStatus() : AppointmentStatus.CONFIRMED)
                .priceAtBooking(service.getPrice())
                .notes(request.getNotes())
                .createdByAdmin(true)
                .build();

        appointment = appointmentRepository.save(appointment);

        createAudit(appointment, "CREATED", adminEmail, null);

        if (appointment.getClientEmail() != null) {
            notificationService.sendNotification(appointment, NotificationType.CONFIRMATION);
        }

        log.info("Admin appointment created with id: {}", appointment.getId());
        return appointmentMapper.toResponse(appointment);
    }

    public AppointmentDTO.Response update(UUID id, AppointmentDTO.UpdateRequest request, String performedBy) {
        log.info("Updating appointment: {} by: {}", id, performedBy);

        Appointment appointment = appointmentRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento", "id", id));

        Map<String, Object> beforeState = captureState(appointment);

        if (request.getStartTime() != null) {
            LocalDateTime newEndTime = request.getStartTime()
                    .plusMinutes(appointment.getService().getTotalDurationMinutes());

            UUID barberId = request.getBarberId() != null ? request.getBarberId() : appointment.getBarber().getId();

            validateNoConflicts(barberId, request.getStartTime(), newEndTime, id);

            appointment.setStartTime(request.getStartTime());
            appointment.setEndTime(newEndTime);

            if (request.getBarberId() != null && !request.getBarberId().equals(appointment.getBarber().getId())) {
                User newBarber = userRepository.findById(request.getBarberId())
                        .orElseThrow(() -> new ResourceNotFoundException("Barbeiro", "id", request.getBarberId()));
                appointment.setBarber(newBarber);
            }
        }

        if (request.getStatus() != null) {
            appointment.setStatus(request.getStatus());
        }

        if (request.getNotes() != null) {
            appointment.setNotes(request.getNotes());
        }

        appointment = appointmentRepository.save(appointment);

        createAudit(appointment, "UPDATED", performedBy, beforeState);

        log.info("Appointment updated: {}", id);
        return appointmentMapper.toResponse(appointment);
    }

    public AppointmentDTO.PublicResponse cancelByClient(String cancellationToken) {
        log.info("Client cancelling appointment with token: {}", cancellationToken);

        Appointment appointment = appointmentRepository.findByCancellationToken(cancellationToken)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento", "token", cancellationToken));

        if (!appointment.isActive()) {
            throw new BusinessException("ALREADY_CANCELLED", "Agendamento já foi cancelado ou finalizado");
        }

        LocalDateTime now = LocalDateTime.now();
        if (!appointment.canBeCancelledByClient(now, clientCancelHours)) {
            throw new BusinessException("CANCELLATION_DEADLINE",
                    String.format("Cancelamento permitido até %d horas antes do horário", clientCancelHours));
        }

        Map<String, Object> beforeState = captureState(appointment);
        appointment.setStatus(AppointmentStatus.CANCELLED_BY_CLIENT);
        appointment = appointmentRepository.save(appointment);

        createAudit(appointment, "CANCELLED_BY_CLIENT", "client", beforeState);

        notificationService.sendNotification(appointment, NotificationType.CANCELLATION);

        log.info("Appointment cancelled by client: {}", appointment.getId());
        return appointmentMapper.toPublicResponse(appointment);
    }

    public AppointmentDTO.Response cancelByAdmin(UUID id, String adminEmail) {
        log.info("Admin {} cancelling appointment: {}", adminEmail, id);

        Appointment appointment = appointmentRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento", "id", id));

        if (!appointment.isActive()) {
            throw new BusinessException("ALREADY_CANCELLED", "Agendamento já foi cancelado ou finalizado");
        }

        Map<String, Object> beforeState = captureState(appointment);
        appointment.setStatus(AppointmentStatus.CANCELLED_BY_ADMIN);
        appointment = appointmentRepository.save(appointment);

        createAudit(appointment, "CANCELLED_BY_ADMIN", adminEmail, beforeState);

        notificationService.sendNotification(appointment, NotificationType.CANCELLATION);

        log.info("Appointment cancelled by admin: {}", id);
        return appointmentMapper.toResponse(appointment);
    }

    public AppointmentDTO.PublicResponse rescheduleByClient(String cancellationToken,
                                                             AppointmentDTO.RescheduleRequest request) {
        log.info("Client rescheduling appointment with token: {}", cancellationToken);

        Appointment appointment = appointmentRepository.findByCancellationToken(cancellationToken)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento", "token", cancellationToken));

        if (!appointment.isActive()) {
            throw new BusinessException("INVALID_STATUS", "Agendamento não pode ser reagendado");
        }

        LocalDateTime now = LocalDateTime.now();
        if (!appointment.canBeCancelledByClient(now, clientCancelHours)) {
            throw new BusinessException("RESCHEDULE_DEADLINE",
                    String.format("Reagendamento permitido até %d horas antes do horário", clientCancelHours));
        }

        UUID barberId = request.getNewBarberId() != null ?
                request.getNewBarberId() : appointment.getBarber().getId();

        if (request.getNewBarberId() != null && !request.getNewBarberId().equals(appointment.getBarber().getId())) {
            User newBarber = userRepository.findById(request.getNewBarberId())
                    .orElseThrow(() -> new ResourceNotFoundException("Barbeiro", "id", request.getNewBarberId()));
            appointment.setBarber(newBarber);
        }

        validateBookingTime(request.getNewStartTime(), barberId, appointment.getService());

        LocalDateTime newEndTime = request.getNewStartTime()
                .plusMinutes(appointment.getService().getTotalDurationMinutes());

        validateNoConflicts(barberId, request.getNewStartTime(), newEndTime, appointment.getId());

        Map<String, Object> beforeState = captureState(appointment);

        appointment.setStartTime(request.getNewStartTime());
        appointment.setEndTime(newEndTime);
        appointment = appointmentRepository.save(appointment);

        createAudit(appointment, "RESCHEDULED", "client", beforeState);

        notificationService.sendNotification(appointment, NotificationType.RESCHEDULE);

        log.info("Appointment rescheduled by client: {}", appointment.getId());
        return appointmentMapper.toPublicResponse(appointment);
    }

    public AppointmentDTO.Response markAsCompleted(UUID id, String adminEmail) {
        return updateStatus(id, AppointmentStatus.COMPLETED, adminEmail);
    }

    public AppointmentDTO.Response markAsNoShow(UUID id, String adminEmail) {
        return updateStatus(id, AppointmentStatus.NO_SHOW, adminEmail);
    }

    @Transactional(readOnly = true)
    public AppointmentDTO.Response findById(UUID id) {
        Appointment appointment = appointmentRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento", "id", id));
        return appointmentMapper.toResponse(appointment);
    }

    @Transactional(readOnly = true)
    public AppointmentDTO.PublicResponse findByToken(String token) {
        Appointment appointment = appointmentRepository.findByCancellationToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento", "token", token));
        return appointmentMapper.toPublicResponse(appointment);
    }

    @Transactional(readOnly = true)
    public List<AppointmentDTO.CalendarEvent> getCalendarEvents(LocalDate startDate, LocalDate endDate, UUID barberId) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        List<Appointment> appointments;
        if (barberId != null) {
            appointments = appointmentRepository.findByBarberIdAndDateRange(barberId, start, end);
        } else {
            appointments = appointmentRepository.findByDateRange(start, end);
        }

        return appointments.stream()
                .map(appointmentMapper::toCalendarEvent)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AppointmentDTO.Response> getUpcoming(UUID barberId) {
        LocalDateTime now = LocalDateTime.now();
        return appointmentRepository.findUpcomingByBarberId(barberId, now).stream()
                .map(appointmentMapper::toResponse)
                .toList();
    }

    private AppointmentDTO.Response updateStatus(UUID id, AppointmentStatus status, String performedBy) {
        Appointment appointment = appointmentRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento", "id", id));

        Map<String, Object> beforeState = captureState(appointment);
        appointment.setStatus(status);
        appointment = appointmentRepository.save(appointment);

        createAudit(appointment, "STATUS_CHANGED_TO_" + status.name(), performedBy, beforeState);

        return appointmentMapper.toResponse(appointment);
    }

    private void validateBookingTime(LocalDateTime startTime, UUID barberId, Service service) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime minTime = now.plusHours(minAdvanceHours);
        LocalDate maxDate = now.toLocalDate().plusDays(maxDaysAhead);

        if (startTime.isBefore(minTime)) {
            throw new BusinessException("MIN_ADVANCE_TIME",
                    String.format("Agendamento deve ser feito com pelo menos %d hora(s) de antecedência", minAdvanceHours));
        }

        if (startTime.toLocalDate().isAfter(maxDate)) {
            throw new BusinessException("MAX_DAYS_AHEAD",
                    String.format("Agendamento permitido até %d dias no futuro", maxDaysAhead));
        }

        DayOfWeekEnum dayOfWeek = DayOfWeekEnum.fromJavaDayOfWeek(startTime.getDayOfWeek());
        WorkingHours workingHours = workingHoursRepository
                .findByBarberIdAndDayOfWeek(barberId, dayOfWeek)
                .orElseThrow(() -> new BusinessException("NOT_WORKING_DAY", "Barbeiro não trabalha neste dia"));

        if (!workingHours.isWorking()) {
            throw new BusinessException("NOT_WORKING_DAY", "Barbeiro não trabalha neste dia");
        }

        LocalDateTime endTime = startTime.plusMinutes(service.getTotalDurationMinutes());

        if (startTime.toLocalTime().isBefore(workingHours.getStartTime()) ||
            endTime.toLocalTime().isAfter(workingHours.getEndTime())) {
            throw new BusinessException("OUTSIDE_WORKING_HOURS", "Horário fora do expediente do barbeiro");
        }
    }

    private void validateNoConflicts(UUID barberId, LocalDateTime start, LocalDateTime end, UUID excludeId) {
        List<Appointment> overlapping;
        if (excludeId != null) {
            overlapping = appointmentRepository.findOverlappingAppointmentsExcluding(barberId, start, end, excludeId);
        } else {
            overlapping = appointmentRepository.findOverlappingAppointments(barberId, start, end);
        }

        if (!overlapping.isEmpty()) {
            throw new ConflictException("Já existe um agendamento neste horário");
        }

        List<TimeBlock> blocks = timeBlockRepository.findOverlappingBlocks(barberId, start, end);
        if (!blocks.isEmpty()) {
            throw new ConflictException("Horário bloqueado pelo barbeiro");
        }
    }

    private User findAvailableBarber(List<User> barbers, Service service, LocalDateTime startTime) {
        LocalDateTime endTime = startTime.plusMinutes(service.getTotalDurationMinutes());

        for (User barber : barbers) {
            try {
                validateBookingTime(startTime, barber.getId(), service);
                validateNoConflicts(barber.getId(), startTime, endTime, null);
                return barber;
            } catch (BusinessException e) {
                // barber not available, try next
            }
        }

        throw new BusinessException("NO_AVAILABILITY", "Nenhum barbeiro disponível neste horário");
    }

    private Map<String, Object> captureState(Appointment appointment) {
        Map<String, Object> state = new HashMap<>();
        state.put("status", appointment.getStatus().name());
        state.put("startTime", appointment.getStartTime().toString());
        state.put("endTime", appointment.getEndTime().toString());
        state.put("barberId", appointment.getBarber().getId().toString());
        state.put("notes", appointment.getNotes());
        return state;
    }

    private void createAudit(Appointment appointment, String action, String performedBy,
                             Map<String, Object> beforeState) {
        AppointmentAudit audit = AppointmentAudit.builder()
                .appointment(appointment)
                .action(action)
                .performedBy(performedBy)
                .performedAt(LocalDateTime.now())
                .beforeState(beforeState)
                .afterState(captureState(appointment))
                .build();

        auditRepository.save(audit);
    }
}
