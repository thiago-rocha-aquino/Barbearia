package com.barbearia.unit;

import com.barbearia.application.dto.AvailabilityDTO;
import com.barbearia.application.service.AvailabilityService;
import com.barbearia.domain.entity.*;
import com.barbearia.domain.enums.AppointmentStatus;
import com.barbearia.domain.enums.DayOfWeekEnum;
import com.barbearia.domain.enums.UserRole;
import com.barbearia.domain.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvailabilityServiceTest {

    @Mock
    private ServiceRepository serviceRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private TimeBlockRepository timeBlockRepository;
    @Mock
    private WorkingHoursRepository workingHoursRepository;

    @InjectMocks
    private AvailabilityService availabilityService;

    private User barber;
    private Service service;
    private WorkingHours workingHours;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(availabilityService, "minAdvanceHours", 1);
        ReflectionTestUtils.setField(availabilityService, "maxDaysAhead", 30);
        ReflectionTestUtils.setField(availabilityService, "slotDurationMinutes", 15);

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
    @DisplayName("Deve retornar slots disponíveis para um dia de trabalho")
    void shouldReturnAvailableSlotsForWorkingDay() {
        LocalDate date = LocalDate.now().plusDays(1);
        DayOfWeekEnum dayOfWeek = DayOfWeekEnum.fromJavaDayOfWeek(date.getDayOfWeek());
        workingHours.setDayOfWeek(dayOfWeek);

        when(serviceRepository.findById(service.getId())).thenReturn(Optional.of(service));
        when(userRepository.findById(barber.getId())).thenReturn(Optional.of(barber));
        when(workingHoursRepository.findByBarberIdAndDayOfWeek(barber.getId(), dayOfWeek))
                .thenReturn(Optional.of(workingHours));
        when(appointmentRepository.findOverlappingAppointments(any(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(timeBlockRepository.findOverlappingBlocks(any(), any(), any()))
                .thenReturn(Collections.emptyList());

        List<AvailabilityDTO.TimeSlot> slots = availabilityService
                .getAvailableSlots(service.getId(), barber.getId(), date);

        assertThat(slots).isNotEmpty();
        assertThat(slots.stream().filter(AvailabilityDTO.TimeSlot::isAvailable).count())
                .isGreaterThan(0);
    }

    @Test
    @DisplayName("Deve marcar slot como indisponível quando há agendamento")
    void shouldMarkSlotAsUnavailableWhenAppointmentExists() {
        LocalDate date = LocalDate.now().plusDays(1);
        DayOfWeekEnum dayOfWeek = DayOfWeekEnum.fromJavaDayOfWeek(date.getDayOfWeek());
        workingHours.setDayOfWeek(dayOfWeek);

        LocalDateTime appointmentStart = date.atTime(10, 0);
        Appointment existingAppointment = Appointment.builder()
                .barber(barber)
                .service(service)
                .startTime(appointmentStart)
                .endTime(appointmentStart.plusMinutes(30))
                .status(AppointmentStatus.CONFIRMED)
                .build();
        ReflectionTestUtils.setField(existingAppointment, "id", UUID.randomUUID());

        when(serviceRepository.findById(service.getId())).thenReturn(Optional.of(service));
        when(userRepository.findById(barber.getId())).thenReturn(Optional.of(barber));
        when(workingHoursRepository.findByBarberIdAndDayOfWeek(barber.getId(), dayOfWeek))
                .thenReturn(Optional.of(workingHours));
        when(appointmentRepository.findOverlappingAppointments(any(), any(), any()))
                .thenReturn(List.of(existingAppointment));
        when(timeBlockRepository.findOverlappingBlocks(any(), any(), any()))
                .thenReturn(Collections.emptyList());

        List<AvailabilityDTO.TimeSlot> slots = availabilityService
                .getAvailableSlots(service.getId(), barber.getId(), date);

        Optional<AvailabilityDTO.TimeSlot> slot10h = slots.stream()
                .filter(s -> s.getTime().equals(LocalTime.of(10, 0)))
                .findFirst();

        assertThat(slot10h).isPresent();
        assertThat(slot10h.get().isAvailable()).isFalse();
    }

    @Test
    @DisplayName("Deve retornar lista vazia para dia não trabalhado")
    void shouldReturnEmptyListForNonWorkingDay() {
        LocalDate date = LocalDate.now().plusDays(1);
        DayOfWeekEnum dayOfWeek = DayOfWeekEnum.fromJavaDayOfWeek(date.getDayOfWeek());

        when(serviceRepository.findById(service.getId())).thenReturn(Optional.of(service));
        when(userRepository.findById(barber.getId())).thenReturn(Optional.of(barber));
        when(workingHoursRepository.findByBarberIdAndDayOfWeek(barber.getId(), dayOfWeek))
                .thenReturn(Optional.empty());

        List<AvailabilityDTO.TimeSlot> slots = availabilityService
                .getAvailableSlots(service.getId(), barber.getId(), date);

        assertThat(slots).isEmpty();
    }

    @Test
    @DisplayName("Deve marcar slot como indisponível quando há bloqueio")
    void shouldMarkSlotAsUnavailableWhenTimeBlockExists() {
        LocalDate date = LocalDate.now().plusDays(1);
        DayOfWeekEnum dayOfWeek = DayOfWeekEnum.fromJavaDayOfWeek(date.getDayOfWeek());
        workingHours.setDayOfWeek(dayOfWeek);

        LocalDateTime blockStart = date.atTime(12, 0);
        TimeBlock timeBlock = TimeBlock.builder()
                .barber(barber)
                .startTime(blockStart)
                .endTime(blockStart.plusHours(1))
                .reason("Almoço")
                .build();
        ReflectionTestUtils.setField(timeBlock, "id", UUID.randomUUID());

        when(serviceRepository.findById(service.getId())).thenReturn(Optional.of(service));
        when(userRepository.findById(barber.getId())).thenReturn(Optional.of(barber));
        when(workingHoursRepository.findByBarberIdAndDayOfWeek(barber.getId(), dayOfWeek))
                .thenReturn(Optional.of(workingHours));
        when(appointmentRepository.findOverlappingAppointments(any(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(timeBlockRepository.findOverlappingBlocks(any(), any(), any()))
                .thenReturn(List.of(timeBlock));

        List<AvailabilityDTO.TimeSlot> slots = availabilityService
                .getAvailableSlots(service.getId(), barber.getId(), date);

        Optional<AvailabilityDTO.TimeSlot> slot12h = slots.stream()
                .filter(s -> s.getTime().equals(LocalTime.of(12, 0)))
                .findFirst();

        assertThat(slot12h).isPresent();
        assertThat(slot12h.get().isAvailable()).isFalse();
    }
}
