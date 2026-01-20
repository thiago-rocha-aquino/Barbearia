package com.barbearia.api.controller;

import com.barbearia.application.dto.AppointmentDTO;
import com.barbearia.application.dto.UserDTO;
import com.barbearia.application.dto.WorkingHoursDTO;
import com.barbearia.application.service.AppointmentService;
import com.barbearia.application.service.UserService;
import com.barbearia.application.service.WorkingHoursService;
import com.barbearia.domain.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/me")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'BARBER')")
@Tag(name = "Perfil", description = "Gerenciamento do próprio perfil")
public class MeController {

    private final UserService userService;
    private final WorkingHoursService workingHoursService;
    private final AppointmentService appointmentService;

    @GetMapping
    @Operation(summary = "Obter perfil", description = "Retorna os dados do usuário logado")
    public ResponseEntity<UserDTO.Response> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername());
        return ResponseEntity.ok(userService.findById(user.getId()));
    }

    @PutMapping
    @Operation(summary = "Atualizar perfil")
    public ResponseEntity<UserDTO.Response> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UserDTO.UpdateRequest request) {
        User user = userService.findByEmail(userDetails.getUsername());
        return ResponseEntity.ok(userService.update(user.getId(), request));
    }

    @PutMapping("/password")
    @Operation(summary = "Alterar senha")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UserDTO.ChangePasswordRequest request) {
        User user = userService.findByEmail(userDetails.getUsername());
        userService.changePassword(user.getId(), request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/working-hours")
    @Operation(summary = "Obter horários de trabalho")
    public ResponseEntity<List<WorkingHoursDTO.Response>> getWorkingHours(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername());
        return ResponseEntity.ok(workingHoursService.findByBarberId(user.getId()));
    }

    @PutMapping("/working-hours")
    @Operation(summary = "Definir horários de trabalho")
    public ResponseEntity<List<WorkingHoursDTO.Response>> setWorkingHours(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody WorkingHoursDTO.BulkRequest request) {
        User user = userService.findByEmail(userDetails.getUsername());
        return ResponseEntity.ok(workingHoursService.setWorkingHours(user.getId(), request));
    }

    @GetMapping("/appointments/upcoming")
    @Operation(summary = "Próximos agendamentos")
    public ResponseEntity<List<AppointmentDTO.Response>> getUpcomingAppointments(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername());
        return ResponseEntity.ok(appointmentService.getUpcoming(user.getId()));
    }
}
