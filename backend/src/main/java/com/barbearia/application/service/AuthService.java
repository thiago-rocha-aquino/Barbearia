package com.barbearia.application.service;

import com.barbearia.application.dto.AuthDTO;
import com.barbearia.application.exception.UnauthorizedException;
import com.barbearia.domain.entity.User;
import com.barbearia.domain.repository.UserRepository;
import com.barbearia.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${app.jwt.expiration:86400000}")
    private long jwtExpiration;

    public AuthDTO.LoginResponse login(AuthDTO.LoginRequest request) {
        log.info("Login attempt for: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Credenciais inválidas"));

        if (!user.isActive()) {
            throw new UnauthorizedException("Usuário desativado");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Invalid password for user: {}", request.getEmail());
            throw new UnauthorizedException("Credenciais inválidas");
        }

        String token = jwtService.generateToken(user);

        log.info("Login successful for: {}", request.getEmail());

        return AuthDTO.LoginResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtExpiration / 1000)
                .user(AuthDTO.UserInfo.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .email(user.getEmail())
                        .role(user.getRole())
                        .build())
                .build();
    }

    public AuthDTO.LoginResponse refreshToken(String token) {
        String email = jwtService.extractUsername(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Token inválido"));

        if (!user.isActive()) {
            throw new UnauthorizedException("Usuário desativado");
        }

        String newToken = jwtService.generateToken(user);

        return AuthDTO.LoginResponse.builder()
                .accessToken(newToken)
                .tokenType("Bearer")
                .expiresIn(jwtExpiration / 1000)
                .user(AuthDTO.UserInfo.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .email(user.getEmail())
                        .role(user.getRole())
                        .build())
                .build();
    }
}
