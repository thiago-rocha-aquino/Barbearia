package com.barbearia.application.mapper;

import com.barbearia.application.dto.UserDTO;
import com.barbearia.domain.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDTO.Response toResponse(User user) {
        return UserDTO.Response.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .active(user.isActive())
                .build();
    }

    public UserDTO.PublicBarberResponse toPublicBarberResponse(User user) {
        return UserDTO.PublicBarberResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .build();
    }
}
