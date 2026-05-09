package com.example.trainapplication.contracts;

public record StopRequest(
        String stationName,
        String arrivalTime,
        String departureTime,
        Integer stopOrder
) {}