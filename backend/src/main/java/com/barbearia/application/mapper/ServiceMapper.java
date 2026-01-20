package com.barbearia.application.mapper;

import com.barbearia.application.dto.ServiceDTO;
import com.barbearia.domain.entity.Service;
import org.springframework.stereotype.Component;

@Component
public class ServiceMapper {

    public Service toEntity(ServiceDTO.Request request) {
        return Service.builder()
                .name(request.getName())
                .description(request.getDescription())
                .durationMinutes(request.getDurationMinutes())
                .bufferMinutes(request.getBufferMinutes() != null ? request.getBufferMinutes() : 0)
                .price(request.getPrice())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .active(request.getActive() != null ? request.getActive() : true)
                .build();
    }

    public void updateEntity(Service service, ServiceDTO.Request request) {
        service.setName(request.getName());
        service.setDescription(request.getDescription());
        service.setDurationMinutes(request.getDurationMinutes());
        service.setBufferMinutes(request.getBufferMinutes() != null ? request.getBufferMinutes() : 0);
        service.setPrice(request.getPrice());
        if (request.getDisplayOrder() != null) {
            service.setDisplayOrder(request.getDisplayOrder());
        }
        if (request.getActive() != null) {
            service.setActive(request.getActive());
        }
    }

    public ServiceDTO.Response toResponse(Service service) {
        return ServiceDTO.Response.builder()
                .id(service.getId())
                .name(service.getName())
                .description(service.getDescription())
                .durationMinutes(service.getDurationMinutes())
                .bufferMinutes(service.getBufferMinutes())
                .totalDurationMinutes(service.getTotalDurationMinutes())
                .price(service.getPrice())
                .displayOrder(service.getDisplayOrder())
                .active(service.isActive())
                .build();
    }

    public ServiceDTO.PublicResponse toPublicResponse(Service service) {
        return ServiceDTO.PublicResponse.builder()
                .id(service.getId())
                .name(service.getName())
                .description(service.getDescription())
                .durationMinutes(service.getDurationMinutes())
                .price(service.getPrice())
                .build();
    }
}
