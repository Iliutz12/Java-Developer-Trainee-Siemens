package com.example.trainapplication.dtos;

import com.example.trainapplication.enums.Role;
import com.example.trainapplication.model.User;

public class UserDtos {

    public record UserResponse(
            Long id,
            String username,
            String email,
            Role role
    ) {
        public static UserResponse fromEntity(User entity) {
            return new UserResponse(
                    entity.getId(),
                    entity.getUsername(),
                    entity.getEmail(),
                    entity.getRole());
        }

    }
}
