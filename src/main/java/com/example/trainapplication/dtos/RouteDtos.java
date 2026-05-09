package com.example.trainapplication.dtos;

import java.time.LocalTime;
import java.util.List;

public class RouteDtos {

    public record RouteSegment(
            Long trainId,
            String trainName,
            String fromStation,
            String toStation,
            LocalTime departure,
            LocalTime arrival
    ) {}

    public record RouteSearchResponse(List<RouteSegment> segments) {
        public boolean requiresChangeover() {
            return segments.size() > 1;
        }
    }
}
