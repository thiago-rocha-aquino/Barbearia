package com.barbearia.application.service;

import com.barbearia.application.dto.UserDTO;
import com.barbearia.application.exception.BusinessException;
import com.barbearia.application.exception.ResourceNotFoundException;
import com.barbearia.application.mapper.UserMapper;
import com.barbearia.domain.entity.User;
import com.barbearia.domain.enums.UserRole;
import com.barbearia.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserDTO.Response create(UserDTO.CreateRequest request) {
        log.info("Creating user: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("EMAIL_EXISTS", "Email já cadastrado");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(request.getRole())
                .active(true)
                .build();

        user = userRepository.save(user);
        log.info("User created with id: {}", user.getId());

        return userMapper.toResponse(user);
    }

    public UserDTO.Response update(UUID id, UserDTO.UpdateRequest request) {
        log.info("Updating user: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", "id", id));

        user.setName(request.getName());
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getActive() != null) {
            user.setActive(request.getActive());
        }

        user = userRepository.save(user);
        log.info("User updated: {}", id);

        return userMapper.toResponse(user);
    }

    public void changePassword(UUID id, UserDTO.ChangePasswordRequest request) {
        log.info("Changing password for user: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", "id", id));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BusinessException("INVALID_PASSWORD", "Senha atual incorreta");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password changed for user: {}", id);
    }

    @Transactional(readOnly = true)
    public UserDTO.Response findById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", "id", id));
        return userMapper.toResponse(user);
    }

    @Transactional(readOnly = true)
    public List<UserDTO.Response> findAll() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserDTO.Response> findBarbers() {
        return userRepository.findAllActiveBarbers().stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserDTO.PublicBarberResponse> findActiveBarbers() {
        return userRepository.findAllActiveBarbers().stream()
                .map(userMapper::toPublicBarberResponse)
                .toList();
    }

    public void delete(UUID id) {
        log.info("Deleting user: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", "id", id));

        if (!user.getAppointments().isEmpty()) {
            user.setActive(false);
            userRepository.save(user);
            log.info("User deactivated (has appointments): {}", id);
        } else {
            userRepository.delete(user);
            log.info("User deleted: {}", id);
        }
    }

    @Transactional(readOnly = true)
    public User getEntityById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", "id", id));
    }

    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", "email", email));
    }

    public User createDefaultAdmin(String email, String password, String name) {
        if (userRepository.existsByEmail(email)) {
            log.info("Admin user already exists: {}", email);
            return userRepository.findByEmail(email).orElseThrow();
        }

        log.info("Creating default admin user: {}", email);
        User admin = User.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(UserRole.ADMIN)
                .active(true)
                .build();

        return userRepository.save(admin);
    }
}
