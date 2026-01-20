package com.barbearia.api.controller;

import com.barbearia.application.dto.ServiceDTO;
import com.barbearia.application.service.ServiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/services")
@RequiredArgsConstructor
@Tag(name = "Serviços (Público)", description = "Listagem pública de serviços")
public class PublicServiceController {

    private final ServiceService serviceService;

    @GetMapping
    @Operation(summary = "Listar serviços ativos", description = "Retorna todos os serviços disponíveis para agendamento")
    public ResponseEntity<List<ServiceDTO.PublicResponse>> listServices() {
        return ResponseEntity.ok(serviceService.findAllActive());
    }
}
