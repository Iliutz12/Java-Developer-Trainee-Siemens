package com.example.trainapplication.contracts;

import com.example.trainapplication.model.Booking;
import com.example.trainapplication.model.Train;
import com.example.trainapplication.model.User;

import java.time.LocalTime;

public record BookingRequest(
        Long userId,
        Long trainId,
        Integer numberOfTickets,
        String departureStation, // <-- NEW
        String arrivalStation,   // <-- NEW
        LocalTime departureTime, // <-- NEW
        LocalTime arrivalTime    // <-- NEW
) {
    public static Booking toEntity(BookingRequest request) {
        User user = new User();
        user.setId(request.userId());

        Train train = new Train();
        train.setId(request.trainId());

        Booking booking = new Booking(user, train, request.numberOfTickets());

        // Map the new fields
        booking.setDepartureStation(request.departureStation());
        booking.setArrivalStation(request.arrivalStation());
        booking.setDepartureTime(request.departureTime());
        booking.setArrivalTime(request.arrivalTime());

        return booking;
    }
}