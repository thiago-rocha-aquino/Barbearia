package com.barbearia.api.controller;

import com.barbearia.application.dto.UserDTO;
import com.barbearia.application.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/barbers")
@RequiredArgsConstructor
@Tag(name = "Barbeiros (Público)", description = "Listagem pública de barbeiros")
public class PublicBarberController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "Listar barbeiros ativos", description = "Retorna todos os barbeiros disponíveis")
    public ResponseEntity<List<UserDTO.PublicBarberResponse>> listBarbers() {
        return ResponseEntity.ok(userService.findActiveBarbers());
    }
}
