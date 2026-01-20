package com.barbearia.application.service;

import com.barbearia.application.dto.WorkingHoursDTO;
import com.barbearia.application.exception.BusinessException;
import com.barbearia.application.exception.ResourceNotFoundException;
import com.barbearia.application.mapper.WorkingHoursMapper;
import com.barbearia.domain.entity.User;
import com.barbearia.domain.entity.WorkingHours;
import com.barbearia.domain.enums.DayOfWeekEnum;
import com.barbearia.domain.repository.UserRepository;
import com.barbearia.domain.repository.WorkingHoursRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class WorkingHoursService {

    private final WorkingHoursRepository workingHoursRepository;
    private final UserRepository userRepository;
    private final WorkingHoursMapper workingHoursMapper;

    public List<WorkingHoursDTO.Response> setWorkingHours(UUID barberId, WorkingHoursDTO.BulkRequest request) {
        log.info("Setting working hours for barber: {}", barberId);

        User barber = userRepository.findById(barberId)
                .orElseThrow(() -> new ResourceNotFoundException("Barbeiro", "id", barberId));

        if (!barber.isBarber()) {
            throw new BusinessException("NOT_BARBER", "Usuário não é um barbeiro");
        }

        for (WorkingHoursDTO.Request wh : request.getWorkingHours()) {
            validateWorkingHours(wh);

            Optional<WorkingHours> existing = workingHoursRepository
                    .findByBarberIdAndDayOfWeek(barberId, wh.getDayOfWeek());

            if (existing.isPresent()) {
                WorkingHours workingHours = existing.get();
                workingHours.setStartTime(wh.getStartTime());
                workingHours.setEndTime(wh.getEndTime());
                workingHours.setWorking(wh.isWorking());
                workingHoursRepository.save(workingHours);
            } else {
                WorkingHours workingHours = WorkingHours.builder()
                        .barber(barber)
                        .dayOfWeek(wh.getDayOfWeek())
                        .startTime(wh.getStartTime())
                        .endTime(wh.getEndTime())
                        .isWorking(wh.isWorking())
                        .build();
                workingHoursRepository.save(workingHours);
            }
        }

        log.info("Working hours set for barber: {}", barberId);
        return findByBarberId(barberId);
    }

    @Transactional(readOnly = true)
    public List<WorkingHoursDTO.Response> findByBarberId(UUID barberId) {
        return workingHoursRepository.findByBarberId(barberId).stream()
                .map(workingHoursMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<WorkingHours> findByBarberAndDay(UUID barberId, DayOfWeekEnum dayOfWeek) {
        return workingHoursRepository.findByBarberIdAndDayOfWeek(barberId, dayOfWeek);
    }

    public void createDefaultWorkingHours(User barber) {
        log.info("Creating default working hours for barber: {}", barber.getId());

        LocalTime defaultStart = LocalTime.of(9, 0);
        LocalTime defaultEnd = LocalTime.of(18, 0);

        for (DayOfWeekEnum day : DayOfWeekEnum.values()) {
            boolean isWorkingDay = day != DayOfWeekEnum.SUNDAY;

            WorkingHours workingHours = WorkingHours.builder()
                    .barber(barber)
                    .dayOfWeek(day)
                    .startTime(defaultStart)
                    .endTime(defaultEnd)
                    .isWorking(isWorkingDay)
                    .build();

            workingHoursRepository.save(workingHours);
        }

        log.info("Default working hours created for barber: {}", barber.getId());
    }

    private void validateWorkingHours(WorkingHoursDTO.Request request) {
        if (request.isWorking() && !request.getStartTime().isBefore(request.getEndTime())) {
            throw new BusinessException("INVALID_TIME_RANGE",
                    "Horário de início deve ser anterior ao horário de término");
        }
    }
}
