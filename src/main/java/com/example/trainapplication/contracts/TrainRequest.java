package com.example.trainapplication.contracts;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TrainRequest(
        @NotBlank(message = "Train name is required")
        String name,

        @NotNull(message = "Total capacity is required")
        @Min(value = 1, message = "Capacity must be at least 1")
        Integer totalCapacity
) {}
