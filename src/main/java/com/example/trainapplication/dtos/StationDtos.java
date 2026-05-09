package com.example.trainapplication.dtos;

import com.example.trainapplication.model.Station;

public class StationDtos {

    public record StationResponse(Long id, String name) {
        public static StationResponse fromEntity(Station station) {
            return new StationResponse(station.getId(), station.getName());
        }
    }
}
