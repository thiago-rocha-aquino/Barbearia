package com.barbearia.api.controller;

import com.barbearia.application.dto.ServiceDTO;
import com.barbearia.application.service.ServiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/services")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'BARBER')")
@Tag(name = "Serviços (Admin)", description = "Gestão de serviços")
public class AdminServiceController {

    private final ServiceService serviceService;

    @GetMapping
    @Operation(summary = "Listar todos os serviços")
    public ResponseEntity<List<ServiceDTO.Response>> listAll() {
        return ResponseEntity.ok(serviceService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar serviço por ID")
    public ResponseEntity<ServiceDTO.Response> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(serviceService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Criar serviço")
    public ResponseEntity<ServiceDTO.Response> create(@Valid @RequestBody ServiceDTO.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(serviceService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualizar serviço")
    public ResponseEntity<ServiceDTO.Response> update(
            @PathVariable UUID id,
            @Valid @RequestBody ServiceDTO.Request request) {
        return ResponseEntity.ok(serviceService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Excluir serviço")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        serviceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
