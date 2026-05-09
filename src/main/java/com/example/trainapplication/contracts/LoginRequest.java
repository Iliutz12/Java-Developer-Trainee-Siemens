package com.example.trainapplication.contracts;

public record LoginRequest(
        String username,
        String password
) {}