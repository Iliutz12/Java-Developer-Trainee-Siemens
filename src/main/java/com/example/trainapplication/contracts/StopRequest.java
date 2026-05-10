package com.example.trainapplication.contracts;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StopRequest(
        @NotBlank(message = "Station name is required")
        String stationName,

        String arrivalTime,
        String departureTime,

        @NotNull(message = "Stop order is required")
        Integer stopOrder
) {}
