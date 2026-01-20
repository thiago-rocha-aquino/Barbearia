package com.barbearia.application.mapper;

import com.barbearia.application.dto.WorkingHoursDTO;
import com.barbearia.domain.entity.WorkingHours;
import org.springframework.stereotype.Component;

@Component
public class WorkingHoursMapper {

    public WorkingHoursDTO.Response toResponse(WorkingHours workingHours) {
        return WorkingHoursDTO.Response.builder()
                .id(workingHours.getId())
                .barberId(workingHours.getBarber().getId())
                .dayOfWeek(workingHours.getDayOfWeek())
                .startTime(workingHours.getStartTime())
                .endTime(workingHours.getEndTime())
                .isWorking(workingHours.isWorking())
                .build();
    }
}
