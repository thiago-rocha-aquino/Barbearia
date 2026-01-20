package com.barbearia.application.service;

import com.barbearia.application.dto.TimeBlockDTO;
import com.barbearia.application.exception.BusinessException;
import com.barbearia.application.exception.ResourceNotFoundException;
import com.barbearia.application.mapper.TimeBlockMapper;
import com.barbearia.domain.entity.TimeBlock;
import com.barbearia.domain.entity.User;
import com.barbearia.domain.repository.AppointmentRepository;
import com.barbearia.domain.repository.TimeBlockRepository;
import com.barbearia.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TimeBlockService {

    private final TimeBlockRepository timeBlockRepository;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final TimeBlockMapper timeBlockMapper;

    public TimeBlockDTO.Response create(TimeBlockDTO.Request request) {
        log.info("Creating time block for barber: {}", request.getBarberId());

        User barber = userRepository.findById(request.getBarberId())
                .orElseThrow(() -> new ResourceNotFoundException("Barbeiro", "id", request.getBarberId()));

        if (!barber.isBarber()) {
            throw new BusinessException("NOT_BARBER", "Usuário não é um barbeiro");
        }

        validateTimeBlock(request);

        var overlappingBlocks = timeBlockRepository.findOverlappingBlocks(
                request.getBarberId(), request.getStartTime(), request.getEndTime());

        if (!overlappingBlocks.isEmpty()) {
            throw new BusinessException("OVERLAPPING_BLOCK", "Já existe um bloqueio neste período");
        }

        var overlappingAppointments = appointmentRepository.findOverlappingAppointments(
                request.getBarberId(), request.getStartTime(), request.getEndTime());

        if (!overlappingAppointments.isEmpty()) {
            throw new BusinessException("OVERLAPPING_APPOINTMENT",
                    "Existem agendamentos neste período. Cancele-os primeiro.");
        }

        TimeBlock timeBlock = TimeBlock.builder()
                .barber(barber)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .reason(request.getReason())
                .build();

        timeBlock = timeBlockRepository.save(timeBlock);
        log.info("Time block created with id: {}", timeBlock.getId());

        return timeBlockMapper.toResponse(timeBlock);
    }

    @Transactional(readOnly = true)
    public TimeBlockDTO.Response findById(UUID id) {
        TimeBlock timeBlock = timeBlockRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bloqueio", "id", id));
        return timeBlockMapper.toResponse(timeBlock);
    }

    @Transactional(readOnly = true)
    public List<TimeBlockDTO.Response> findByBarberId(UUID barberId) {
        return timeBlockRepository.findByBarberId(barberId).stream()
                .map(timeBlockMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TimeBlockDTO.Response> findByDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        return timeBlockRepository.findByDateRange(start, end).stream()
                .map(timeBlockMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TimeBlock> findOverlappingBlocks(UUID barberId, LocalDateTime start, LocalDateTime end) {
        return timeBlockRepository.findOverlappingBlocks(barberId, start, end);
    }

    public void delete(UUID id) {
        log.info("Deleting time block: {}", id);

        TimeBlock timeBlock = timeBlockRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bloqueio", "id", id));

        timeBlockRepository.delete(timeBlock);
        log.info("Time block deleted: {}", id);
    }

    private void validateTimeBlock(TimeBlockDTO.Request request) {
        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new BusinessException("INVALID_TIME_RANGE",
                    "Horário de início deve ser anterior ao horário de término");
        }

        if (request.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("PAST_TIME", "Não é possível criar bloqueio no passado");
        }
    }
}
