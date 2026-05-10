package com.example.trainapplication.controller;

import com.example.trainapplication.contracts.BookingRequest;
import com.example.trainapplication.dtos.BookingDtos;
import com.example.trainapplication.model.Booking;
import com.example.trainapplication.services.IBookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        return ResponseEntity.ok(
                bookingService.getBookings().stream()
                        .map(BookingDtos.BookingResponse::fromEntity)
                        .toList()
        );
    }

    @PostMapping
    public ResponseEntity<?> createBooking(@Valid @RequestBody BookingRequest request) {
        try {
            Booking saved = bookingService.createBooking(BookingRequest.toEntity(request));
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(BookingDtos.BookingResponse.fromEntity(saved));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingDtos.BookingResponse> getById(@PathVariable Long id) {
        Booking booking = bookingService.getBookingById(id);
        if (booking == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(BookingDtos.BookingResponse.fromEntity(booking));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookingDtos.BookingResponse> updateBooking(
            @PathVariable Long id,
            @Valid @RequestBody BookingRequest request) {
        Booking updated = bookingService.updateBooking(id, BookingRequest.toEntity(request));
        if (updated == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(BookingDtos.BookingResponse.fromEntity(updated));
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

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingDtos.BookingResponse>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(
                bookingService.getBookingsByUser(userId).stream()
                        .map(BookingDtos.BookingResponse::fromEntity)
                        .toList()
        );
    }

    @GetMapping("/trainid/{trainId}")
    public ResponseEntity<List<BookingDtos.BookingResponse>> getBookingsByTrainId(@PathVariable Long trainId) {
        return ResponseEntity.ok(
                bookingService.getBookingsByTrain(trainId).stream()
                        .map(BookingDtos.BookingResponse::fromEntity)
                        .toList()
        );
    }
}
