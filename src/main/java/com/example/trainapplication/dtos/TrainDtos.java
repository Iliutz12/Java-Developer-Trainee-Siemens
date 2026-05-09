package com.example.trainapplication.dtos;

import com.example.trainapplication.model.Train;

public class TrainDtos {

    public record TrainResponse(
            Long id,
            String name,
            Integer totalCapacity,
            Integer delayMinutes,
            Integer bookedTickets
    ) {
        public static TrainResponse fromEntity(Train train, Integer bookedTickets) {
            return new TrainResponse(
                    train.getId(),
                    train.getName(),
                    train.getTotalCapacity(),
                    train.getDelayMinutes(),
                    bookedTickets
            );
        }
    }
}
