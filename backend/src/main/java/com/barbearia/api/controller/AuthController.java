package com.barbearia.api.controller;

import com.barbearia.application.dto.AuthDTO;
import com.barbearia.application.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Endpoints de autenticação")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Autenticação de usuário")
    public ResponseEntity<AuthDTO.LoginResponse> login(@Valid @RequestBody AuthDTO.LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh Token", description = "Renovar token de acesso")
    public ResponseEntity<AuthDTO.LoginResponse> refresh(@Valid @RequestBody AuthDTO.RefreshRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request.getToken()));
    }
}
