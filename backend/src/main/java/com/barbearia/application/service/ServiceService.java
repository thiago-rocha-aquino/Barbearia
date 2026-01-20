package com.barbearia.application.service;

import com.barbearia.application.dto.ServiceDTO;
import com.barbearia.application.exception.BusinessException;
import com.barbearia.application.exception.ResourceNotFoundException;
import com.barbearia.application.mapper.ServiceMapper;
import com.barbearia.domain.entity.Service;
import com.barbearia.domain.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ServiceService {

    private final ServiceRepository serviceRepository;
    private final ServiceMapper serviceMapper;

    public ServiceDTO.Response create(ServiceDTO.Request request) {
        log.info("Creating service: {}", request.getName());

        if (serviceRepository.existsByName(request.getName())) {
            throw new BusinessException("SERVICE_NAME_EXISTS", "Já existe um serviço com este nome");
        }

        Service service = serviceMapper.toEntity(request);
        service = serviceRepository.save(service);

        log.info("Service created with id: {}", service.getId());
        return serviceMapper.toResponse(service);
    }

    public ServiceDTO.Response update(UUID id, ServiceDTO.Request request) {
        log.info("Updating service: {}", id);

        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Serviço", "id", id));

        if (serviceRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new BusinessException("SERVICE_NAME_EXISTS", "Já existe um serviço com este nome");
        }

        serviceMapper.updateEntity(service, request);
        service = serviceRepository.save(service);

        log.info("Service updated: {}", id);
        return serviceMapper.toResponse(service);
    }

    @Transactional(readOnly = true)
    public ServiceDTO.Response findById(UUID id) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Serviço", "id", id));
        return serviceMapper.toResponse(service);
    }

    @Transactional(readOnly = true)
    public List<ServiceDTO.Response> findAll() {
        return serviceRepository.findAll().stream()
                .map(serviceMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ServiceDTO.PublicResponse> findAllActive() {
        return serviceRepository.findByActiveTrueOrderByDisplayOrderAsc().stream()
                .map(serviceMapper::toPublicResponse)
                .toList();
    }

    public void delete(UUID id) {
        log.info("Deleting service: {}", id);

        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Serviço", "id", id));

        if (!service.getAppointments().isEmpty()) {
            service.setActive(false);
            serviceRepository.save(service);
            log.info("Service deactivated (has appointments): {}", id);
        } else {
            serviceRepository.delete(service);
            log.info("Service deleted: {}", id);
        }
    }

    @Transactional(readOnly = true)
    public Service getEntityById(UUID id) {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Serviço", "id", id));
    }
}
