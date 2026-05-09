package com.example.trainapplication.contracts;

import com.example.trainapplication.enums.Role;
import com.example.trainapplication.model.User;

public record UserRequest(
        String username,
        String email,
        String password,
        Role role
) {
    public static User toEntity(UserRequest request) {
        return new User(request.username(), request.email(), request.password(), request.role());
    }
}