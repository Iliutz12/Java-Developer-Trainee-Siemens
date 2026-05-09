package com.example.trainapplication.contracts;

import jakarta.validation.constraints.NotNull;

public record TrainRequest(
        String name,
        @NotNull Integer totalCapacity
) {}
