package com.example.trainapplication.services;

import com.example.trainapplication.model.Booking;
import com.example.trainapplication.model.User;

import java.util.List;

public interface IBookingService {

    public List<Booking> getBookings();
    public Booking getBookingById(Long id);
    public Booking createBooking(Booking booking);
    public Booking updateBooking(Long id,Booking booking);
    public void deleteBookingById(Long id);
    public Booking getBookingByUser(User user);

}
