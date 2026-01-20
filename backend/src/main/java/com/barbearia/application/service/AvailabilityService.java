package com.barbearia.application.service;

import com.barbearia.application.dto.AvailabilityDTO;
import com.barbearia.application.exception.ResourceNotFoundException;
import com.barbearia.domain.entity.Appointment;
import com.barbearia.domain.entity.Service;
import com.barbearia.domain.entity.TimeBlock;
import com.barbearia.domain.entity.User;
import com.barbearia.domain.entity.WorkingHours;
import com.barbearia.domain.enums.DayOfWeekEnum;
import com.barbearia.domain.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AvailabilityService {

    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final TimeBlockRepository timeBlockRepository;
    private final WorkingHoursRepository workingHoursRepository;

    @Value("${app.booking.min-advance-hours:1}")
    private int minAdvanceHours;

    @Value("${app.booking.max-days-ahead:30}")
    private int maxDaysAhead;

    @Value("${app.booking.slot-duration-minutes:15}")
    private int slotDurationMinutes;

    public List<AvailabilityDTO.TimeSlot> getAvailableSlots(UUID serviceId, UUID barberId, LocalDate date) {
        log.debug("Getting available slots for service: {}, barber: {}, date: {}", serviceId, barberId, date);

        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Serviço", "id", serviceId));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime minDateTime = now.plusHours(minAdvanceHours);
        LocalDate maxDate = now.toLocalDate().plusDays(maxDaysAhead);

        if (date.isBefore(now.toLocalDate()) || date.isAfter(maxDate)) {
            return Collections.emptyList();
        }

        List<User> barbers;
        if (barberId != null) {
            User barber = userRepository.findById(barberId)
                    .orElseThrow(() -> new ResourceNotFoundException("Barbeiro", "id", barberId));
            barbers = List.of(barber);
        } else {
            barbers = userRepository.findAllActiveBarbers();
        }

        List<AvailabilityDTO.TimeSlot> allSlots = new ArrayList<>();

        for (User barber : barbers) {
            List<AvailabilityDTO.TimeSlot> barberSlots = getSlotsForBarber(
                    barber, service, date, minDateTime
            );
            allSlots.addAll(barberSlots);
        }

        allSlots.sort(Comparator.comparing(AvailabilityDTO.TimeSlot::getDateTime)
                .thenComparing(AvailabilityDTO.TimeSlot::getBarberName));

        return allSlots;
    }

    public List<AvailabilityDTO.DayAvailability> getMonthAvailability(UUID serviceId, UUID barberId, int year, int month) {
        log.debug("Getting month availability for service: {}, year: {}, month: {}", serviceId, year, month);

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        LocalDate today = LocalDate.now();
        LocalDate maxDate = today.plusDays(maxDaysAhead);

        if (startDate.isBefore(today)) {
            startDate = today;
        }
        if (endDate.isAfter(maxDate)) {
            endDate = maxDate;
        }

        List<AvailabilityDTO.DayAvailability> days = new ArrayList<>();

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            List<AvailabilityDTO.TimeSlot> slots = getAvailableSlots(serviceId, barberId, date);
            boolean hasAvailable = slots.stream().anyMatch(AvailabilityDTO.TimeSlot::isAvailable);

            days.add(AvailabilityDTO.DayAvailability.builder()
                    .date(date)
                    .hasAvailableSlots(hasAvailable)
                    .slots(null)
                    .build());
        }

        return days;
    }

    private List<AvailabilityDTO.TimeSlot> getSlotsForBarber(User barber, Service service,
                                                              LocalDate date, LocalDateTime minDateTime) {
        DayOfWeekEnum dayOfWeek = DayOfWeekEnum.fromJavaDayOfWeek(date.getDayOfWeek());

        Optional<WorkingHours> workingHoursOpt = workingHoursRepository
                .findByBarberIdAndDayOfWeek(barber.getId(), dayOfWeek);

        if (workingHoursOpt.isEmpty() || !workingHoursOpt.get().isWorking()) {
            return Collections.emptyList();
        }

        WorkingHours workingHours = workingHoursOpt.get();
        LocalTime startTime = workingHours.getStartTime();
        LocalTime endTime = workingHours.getEndTime();

        LocalDateTime dayStart = date.atTime(startTime);
        LocalDateTime dayEnd = date.atTime(endTime);

        List<Appointment> appointments = appointmentRepository.findOverlappingAppointments(
                barber.getId(), dayStart, dayEnd
        );

        List<TimeBlock> blocks = timeBlockRepository.findOverlappingBlocks(
                barber.getId(), dayStart, dayEnd
        );

        int serviceDuration = service.getTotalDurationMinutes();
        List<AvailabilityDTO.TimeSlot> slots = new ArrayList<>();

        LocalDateTime slotTime = dayStart;
        while (slotTime.plusMinutes(serviceDuration).isBefore(dayEnd) ||
               slotTime.plusMinutes(serviceDuration).equals(dayEnd)) {

            LocalDateTime slotEnd = slotTime.plusMinutes(serviceDuration);

            boolean isAvailable = !slotTime.isBefore(minDateTime)
                    && !hasOverlap(slotTime, slotEnd, appointments)
                    && !hasBlockOverlap(slotTime, slotEnd, blocks);

            slots.add(AvailabilityDTO.TimeSlot.builder()
                    .dateTime(slotTime)
                    .time(slotTime.toLocalTime())
                    .available(isAvailable)
                    .barberId(barber.getId())
                    .barberName(barber.getName())
                    .build());

            slotTime = slotTime.plusMinutes(slotDurationMinutes);
        }

        return slots;
    }

    private boolean hasOverlap(LocalDateTime start, LocalDateTime end, List<Appointment> appointments) {
        return appointments.stream().anyMatch(apt -> apt.overlaps(start, end));
    }

    private boolean hasBlockOverlap(LocalDateTime start, LocalDateTime end, List<TimeBlock> blocks) {
        return blocks.stream().anyMatch(block -> block.overlaps(start, end));
    }
}
