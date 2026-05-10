package com.example.trainapplication.contracts;

import com.example.trainapplication.enums.Role;
import com.example.trainapplication.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserRequest(
        @NotBlank(message = "Username is required")
        String username,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        String password,

        @NotNull(message = "Role is required")
        Role role
) {
    public static User toEntity(UserRequest request) {
        return new User(request.username(), request.email(), request.password(), request.role());
    }
}
