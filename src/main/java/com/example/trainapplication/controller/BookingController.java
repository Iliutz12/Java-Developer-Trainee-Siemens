package com.example.trainapplication.controller;

import com.example.trainapplication.contracts.BookingRequest;
import com.example.trainapplication.dtos.BookingDtos;
import com.example.trainapplication.model.Booking;
import com.example.trainapplication.services.IBookingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/bookings")
public class BookingController {

    private final IBookingService bookingService;

    public BookingController(IBookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public ResponseEntity<List<BookingDtos.BookingResponse>> getAllBookings() {
        List<Booking> bookings = bookingService.getBookings();
        List<BookingDtos.BookingResponse> result = new ArrayList<>();
        for (Booking booking : bookings) {
            result.add(BookingDtos.BookingResponse.fromEntity(booking));
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody BookingRequest request) {
        try {
            Booking toCreate = BookingRequest.toEntity(request);
            Booking savedBooking = bookingService.createBooking(toCreate);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(BookingDtos.BookingResponse.fromEntity(savedBooking));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBooking(@PathVariable Long id) {
        try {
            bookingService.deleteBookingById(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingDtos.BookingResponse> getById(@PathVariable Long id) {
        Booking booking = bookingService.getBookingById(id);
        if (booking == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(BookingDtos.BookingResponse.fromEntity(booking));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookingDtos.BookingResponse> updateBooking(
            @PathVariable Long id,
            @RequestBody BookingRequest request) {
        Booking toUpdate = BookingRequest.toEntity(request);
        Booking updatedBooking = bookingService.updateBooking(id, toUpdate);
        if (updatedBooking == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(BookingDtos.BookingResponse.fromEntity(updatedBooking));
    }
}