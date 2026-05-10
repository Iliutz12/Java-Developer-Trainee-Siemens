package com.example.trainapplication.services;

import com.example.trainapplication.model.Booking;
import com.example.trainapplication.model.User;

import java.util.List;

public interface IBookingService {

    List<Booking> getBookings();
    Booking getBookingById(Long id);
    Booking createBooking(Booking booking);
    Booking updateBooking(Long id, Booking booking);
    void deleteBookingById(Long id);
    List<Booking> getBookingsByUser(Long userId);
}
