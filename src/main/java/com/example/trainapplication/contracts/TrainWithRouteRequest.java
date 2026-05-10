package com.example.trainapplication.contracts;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record TrainWithRouteRequest(
        @NotBlank(message = "Train name is required")
        String name,

        @NotNull(message = "Total capacity is required")
        @Min(value = 1, message = "Capacity must be at least 1")
        Integer totalCapacity,

        @NotEmpty(message = "At least one stop is required")
        @Valid
        List<StopRequest> stops
) {}
