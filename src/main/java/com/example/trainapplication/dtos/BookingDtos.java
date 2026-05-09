package com.example.trainapplication.dtos;

import com.example.trainapplication.model.Booking;

import java.time.LocalTime;

public class BookingDtos {

    public record TrainResponse(Long id, String name) {}

    public record BookingResponse(
            Long id,
            UserDtos.UserResponse user,
            TrainResponse train,
            Integer numberOfTickets,
            String departureStation,
            String arrivalStation,
            LocalTime departureTime,
            LocalTime arrivalTime
    ) {
        public static BookingResponse fromEntity(Booking entity) {
            return new BookingResponse(
                    entity.getId(),
                    UserDtos.UserResponse.fromEntity(entity.getUser()),
                    new TrainResponse(entity.getTrain().getId(), entity.getTrain().getName()),
                    entity.getNumberOfTickets(),
                    entity.getDepartureStation(),
                    entity.getArrivalStation(),
                    entity.getDepartureTime(),
                    entity.getArrivalTime()
            );
        }
    }
}