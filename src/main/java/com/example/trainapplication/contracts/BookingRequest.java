package com.example.trainapplication.contracts;

import com.example.trainapplication.model.Booking;
import com.example.trainapplication.model.Train;
import com.example.trainapplication.model.User;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record BookingRequest(
        @NotNull(message = "userId is required")
        Long userId,

        @NotNull(message = "trainId is required")
        Long trainId,

        @NotNull(message = "numberOfTickets is required")
        @Min(value = 1, message = "Must book at least 1 ticket")
        Integer numberOfTickets,

        String departureStation,
        String arrivalStation,
        LocalTime departureTime,
        LocalTime arrivalTime
) {
    public static Booking toEntity(BookingRequest request) {
        User user = new User();
        user.setId(request.userId());

        Train train = new Train();
        train.setId(request.trainId());

        Booking booking = new Booking(user, train, request.numberOfTickets());
        booking.setDepartureStation(request.departureStation());
        booking.setArrivalStation(request.arrivalStation());
        booking.setDepartureTime(request.departureTime());
        booking.setArrivalTime(request.arrivalTime());

        return booking;
    }
}
