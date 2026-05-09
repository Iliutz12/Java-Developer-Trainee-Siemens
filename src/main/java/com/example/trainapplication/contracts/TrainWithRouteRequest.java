package com.example.trainapplication.contracts;

import java.util.List;

public record TrainWithRouteRequest(
        String name,
        Integer totalCapacity,
        List<StopRequest> stops
) {}