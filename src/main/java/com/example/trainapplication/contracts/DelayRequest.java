package com.example.trainapplication.contracts;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record DelayRequest(
        @NotNull(message = "trainId is required")
        Long trainId,

        @NotNull(message = "delayMinutes is required")
        @Min(value = 0, message = "Delay cannot be negative")
        Integer delayMinutes
) {}
