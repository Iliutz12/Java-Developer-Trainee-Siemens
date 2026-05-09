package com.example.trainapplication.controller;

import com.example.trainapplication.contracts.LoginRequest;
import com.example.trainapplication.contracts.UserRequest;
import com.example.trainapplication.dtos.UserDtos;
import com.example.trainapplication.model.User;
import com.example.trainapplication.services.IUserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/home-page")
public class UserController {

    private final IUserService userService;

    public UserController(IUserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserDtos.UserResponse> getAllUsers() {
        List<User> users = userService.getAllUsers();
        List<UserDtos.UserResponse> result = new ArrayList<>();
        for (User user : users) {
            result.add(UserDtos.UserResponse.fromEntity(user));
        }
        return result;
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody UserRequest request) {
        if (userService.getUserByUsername(request.username()) != null ||
                userService.getUserByEmail(request.email()) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Username or email already exists."));
        }

        User toCreate = UserRequest.toEntity(request);
        User savedUser = userService.addUser(toCreate);

        if (savedUser == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Invalid email address."));
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(UserDtos.UserResponse.fromEntity(savedUser));
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest request) {
        User loginUser = userService.authenticate(request.username(), request.password());
        if (loginUser != null) {
            return ResponseEntity.ok(UserDtos.UserResponse.fromEntity(loginUser));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Invalid username or password."));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUserById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDtos.UserResponse> getById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(UserDtos.UserResponse.fromEntity(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserRequest request) {
        User toUpdate = UserRequest.toEntity(request);
        User updatedUser = userService.updateUser(id, toUpdate);

        if (updatedUser == null) {
            if (userService.getUserById(id) == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Username already taken by another user."));
        }
        return ResponseEntity.ok(UserDtos.UserResponse.fromEntity(updatedUser));
    }
}