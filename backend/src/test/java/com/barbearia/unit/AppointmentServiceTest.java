package com.barbearia.unit;

import com.barbearia.application.dto.AppointmentDTO;
import com.barbearia.application.exception.BusinessException;
import com.barbearia.application.exception.ConflictException;
import com.barbearia.application.mapper.AppointmentMapper;
import com.barbearia.application.service.AppointmentService;
import com.barbearia.domain.entity.*;
import com.barbearia.domain.enums.AppointmentStatus;
import com.barbearia.domain.enums.DayOfWeekEnum;
import com.barbearia.domain.enums.UserRole;
import com.barbearia.domain.repository.*;
import com.barbearia.infrastructure.notification.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private ServiceRepository serviceRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TimeBlockRepository timeBlockRepository;
    @Mock
    private WorkingHoursRepository workingHoursRepository;
    @Mock
    private AppointmentAuditRepository auditRepository;
    @Mock
    private AppointmentMapper appointmentMapper;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private AppointmentService appointmentService;

    private User barber;
    private Service service;
    private WorkingHours workingHours;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(appointmentService, "minAdvanceHours", 1);
        ReflectionTestUtils.setField(appointmentService, "maxDaysAhead", 30);
        ReflectionTestUtils.setField(appointmentService, "clientCancelHours", 4);

        barber = User.builder()
                .name("Barbeiro Test")
                .email("barber@test.com")
                .role(UserRole.BARBER)
                .active(true)
                .build();
        ReflectionTestUtils.setField(barber, "id", UUID.randomUUID());

        service = Service.builder()
                .name("Corte")
                .durationMinutes(30)
                .bufferMinutes(0)
                .price(BigDecimal.valueOf(35))
                .active(true)
                .build();
        ReflectionTestUtils.setField(service, "id", UUID.randomUUID());

        workingHours = WorkingHours.builder()
                .barber(barber)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(18, 0))
                .isWorking(true)
                .build();
        ReflectionTestUtils.setField(workingHours, "id", UUID.randomUUID());
    }

    @Test
    @DisplayName("Deve criar agendamento com sucesso")
    void shouldCreateAppointmentSuccessfully() {
        LocalDateTime startTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        DayOfWeekEnum dayOfWeek = DayOfWeekEnum.fromJavaDayOfWeek(startTime.getDayOfWeek());
        workingHours.setDayOfWeek(dayOfWeek);

        AppointmentDTO.PublicCreateRequest request = AppointmentDTO.PublicCreateRequest.builder()
                .serviceId(service.getId())
                .barberId(barber.getId())
                .startTime(startTime)
                .clientName("Cliente Test")
                .clientPhone("11999999999")
                .clientEmail("cliente@test.com")
                .build();

        when(serviceRepository.findById(service.getId())).thenReturn(Optional.of(service));
        when(userRepository.findById(barber.getId())).thenReturn(Optional.of(barber));
        when(workingHoursRepository.findByBarberIdAndDayOfWeek(barber.getId(), dayOfWeek))
                .thenReturn(Optional.of(workingHours));
        when(appointmentRepository.findOverlappingAppointments(any(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(timeBlockRepository.findOverlappingBlocks(any(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(appointmentRepository.save(any())).thenAnswer(inv -> {
            Appointment apt = inv.getArgument(0);
            apt.setId(UUID.randomUUID());
            return apt;
        });
        when(appointmentMapper.toResponse(any())).thenReturn(AppointmentDTO.Response.builder()
                .id(UUID.randomUUID())
                .build());

        AppointmentDTO.Response response = appointmentService.createPublic(request);

        assertThat(response).isNotNull();
        verify(appointmentRepository).save(any());
        verify(notificationService).sendNotification(any(), any());
    }

    @Test
    @DisplayName("Deve rejeitar agendamento com menos de 1 hora de antecedência")
    void shouldRejectAppointmentWithLessThanMinAdvance() {
        LocalDateTime startTime = LocalDateTime.now().plusMinutes(30);

        AppointmentDTO.PublicCreateRequest request = AppointmentDTO.PublicCreateRequest.builder()
                .serviceId(service.getId())
                .barberId(barber.getId())
                .startTime(startTime)
                .clientName("Cliente Test")
                .clientPhone("11999999999")
                .build();

        when(serviceRepository.findById(service.getId())).thenReturn(Optional.of(service));
        when(userRepository.findById(barber.getId())).thenReturn(Optional.of(barber));

        assertThatThrownBy(() -> appointmentService.createPublic(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("antecedência");
    }

    @Test
    @DisplayName("Deve rejeitar agendamento com conflito de horário")
    void shouldRejectAppointmentWithTimeConflict() {
        LocalDateTime startTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        DayOfWeekEnum dayOfWeek = DayOfWeekEnum.fromJavaDayOfWeek(startTime.getDayOfWeek());
        workingHours.setDayOfWeek(dayOfWeek);

        Appointment existingAppointment = Appointment.builder()
                .barber(barber)
                .service(service)
                .startTime(startTime)
                .endTime(startTime.plusMinutes(30))
                .status(AppointmentStatus.CONFIRMED)
                .build();
        ReflectionTestUtils.setField(existingAppointment, "id", UUID.randomUUID());

        AppointmentDTO.PublicCreateRequest request = AppointmentDTO.PublicCreateRequest.builder()
                .serviceId(service.getId())
                .barberId(barber.getId())
                .startTime(startTime)
                .clientName("Cliente Test")
                .clientPhone("11999999999")
                .build();

        when(serviceRepository.findById(service.getId())).thenReturn(Optional.of(service));
        when(userRepository.findById(barber.getId())).thenReturn(Optional.of(barber));
        when(workingHoursRepository.findByBarberIdAndDayOfWeek(barber.getId(), dayOfWeek))
                .thenReturn(Optional.of(workingHours));
        when(appointmentRepository.findOverlappingAppointments(any(), any(), any()))
                .thenReturn(Collections.singletonList(existingAppointment));

        assertThatThrownBy(() -> appointmentService.createPublic(request))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("Deve permitir cancelamento pelo cliente até 4 horas antes")
    void shouldAllowClientCancellationWithin4Hours() {
        LocalDateTime startTime = LocalDateTime.now().plusHours(5);
        String cancellationToken = "test-token";

        Appointment appointment = Appointment.builder()
                .barber(barber)
                .service(service)
                .startTime(startTime)
                .endTime(startTime.plusMinutes(30))
                .status(AppointmentStatus.CONFIRMED)
                .cancellationToken(cancellationToken)
                .build();
        ReflectionTestUtils.setField(appointment, "id", UUID.randomUUID());

        when(appointmentRepository.findByCancellationToken(cancellationToken))
                .thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any())).thenReturn(appointment);
        when(appointmentMapper.toPublicResponse(any())).thenReturn(
                AppointmentDTO.PublicResponse.builder().build());

        AppointmentDTO.PublicResponse response = appointmentService.cancelByClient(cancellationToken);

        assertThat(response).isNotNull();
        verify(appointmentRepository).save(argThat(apt ->
                apt.getStatus() == AppointmentStatus.CANCELLED_BY_CLIENT));
    }

    @Test
    @DisplayName("Deve rejeitar cancelamento pelo cliente com menos de 4 horas")
    void shouldRejectClientCancellationLessThan4Hours() {
        LocalDateTime startTime = LocalDateTime.now().plusHours(2);
        String cancellationToken = "test-token";

        Appointment appointment = Appointment.builder()
                .barber(barber)
                .service(service)
                .startTime(startTime)
                .endTime(startTime.plusMinutes(30))
                .status(AppointmentStatus.CONFIRMED)
                .cancellationToken(cancellationToken)
                .build();
        ReflectionTestUtils.setField(appointment, "id", UUID.randomUUID());

        when(appointmentRepository.findByCancellationToken(cancellationToken))
                .thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> appointmentService.cancelByClient(cancellationToken))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("4 horas");
    }
}
