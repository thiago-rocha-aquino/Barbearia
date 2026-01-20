package com.barbearia.application.mapper;

import com.barbearia.application.dto.TimeBlockDTO;
import com.barbearia.domain.entity.TimeBlock;
import org.springframework.stereotype.Component;

@Component
public class TimeBlockMapper {

    public TimeBlockDTO.Response toResponse(TimeBlock timeBlock) {
        return TimeBlockDTO.Response.builder()
                .id(timeBlock.getId())
                .barberId(timeBlock.getBarber().getId())
                .barberName(timeBlock.getBarber().getName())
                .startTime(timeBlock.getStartTime())
                .endTime(timeBlock.getEndTime())
                .reason(timeBlock.getReason())
                .build();
    }
}
