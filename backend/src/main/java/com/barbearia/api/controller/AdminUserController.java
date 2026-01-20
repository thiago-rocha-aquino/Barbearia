package com.barbearia.api.controller;

import com.barbearia.application.dto.UserDTO;
import com.barbearia.application.dto.WorkingHoursDTO;
import com.barbearia.application.service.UserService;
import com.barbearia.application.service.WorkingHoursService;
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
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Usuários (Admin)", description = "Gestão de usuários e barbeiros")
public class AdminUserController {

    private final UserService userService;
    private final WorkingHoursService workingHoursService;

    @GetMapping
    @Operation(summary = "Listar todos os usuários")
    public ResponseEntity<List<UserDTO.Response>> listAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/barbers")
    @Operation(summary = "Listar barbeiros")
    public ResponseEntity<List<UserDTO.Response>> listBarbers() {
        return ResponseEntity.ok(userService.findBarbers());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuário por ID")
    public ResponseEntity<UserDTO.Response> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Criar usuário")
    public ResponseEntity<UserDTO.Response> create(@Valid @RequestBody UserDTO.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar usuário")
    public ResponseEntity<UserDTO.Response> update(
            @PathVariable UUID id,
            @Valid @RequestBody UserDTO.UpdateRequest request) {
        return ResponseEntity.ok(userService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir usuário")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/working-hours")
    @Operation(summary = "Buscar horários de trabalho")
    public ResponseEntity<List<WorkingHoursDTO.Response>> getWorkingHours(@PathVariable UUID id) {
        return ResponseEntity.ok(workingHoursService.findByBarberId(id));
    }

    @PutMapping("/{id}/working-hours")
    @Operation(summary = "Definir horários de trabalho")
    public ResponseEntity<List<WorkingHoursDTO.Response>> setWorkingHours(
            @PathVariable UUID id,
            @Valid @RequestBody WorkingHoursDTO.BulkRequest request) {
        return ResponseEntity.ok(workingHoursService.setWorkingHours(id, request));
    }
}
