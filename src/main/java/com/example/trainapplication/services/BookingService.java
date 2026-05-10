package com.example.trainapplication.services;

import com.example.trainapplication.model.Booking;
import com.example.trainapplication.model.Train;
import com.example.trainapplication.model.User;
import com.example.trainapplication.repositories.IBookingRepository;
import com.example.trainapplication.repositories.ITrainRepository;
import com.example.trainapplication.repositories.IUserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BookingService implements IBookingService {

    private final IBookingRepository bookingRepository;
    private final ITrainRepository trainRepository;
    private final IUserRepository userRepository;
    private final INotificationService notificationService;

    public BookingService(IBookingRepository bookingRepository,
                          ITrainRepository trainRepository,
                          IUserRepository userRepository,
                          INotificationService notificationService) {
        this.bookingRepository   = bookingRepository;
        this.trainRepository     = trainRepository;
        this.userRepository      = userRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public Booking createBooking(Booking booking) {
        Long userId           = booking.getUser().getId();
        Long trainId          = booking.getTrain().getId();
        Integer requestedTickets = booking.getNumberOfTickets();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Train train = trainRepository.findByIdForUpdate(trainId)
                .orElseThrow(() -> new RuntimeException("Train not found"));

        Integer alreadyBooked = bookingRepository.sumBookedTicketsByTrainId(trainId);
        int booked = (alreadyBooked != null) ? alreadyBooked : 0;
        int available = train.getTotalCapacity() - booked;

        if (requestedTickets > available) {
            throw new RuntimeException(
                    "Not enough seats. Requested: " + requestedTickets + ", Available: " + available);
        }

        booking.setUser(user);
        booking.setTrain(train);
        Booking saved = bookingRepository.save(booking);

        notificationService.sendBookingConfirmation(
                user.getEmail(), user.getUsername(), train.getName(), requestedTickets);

        return saved;
    }

    @Override
    public List<Booking> getBookings() {
        return bookingRepository.findAll();
    }

    @Override
    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id).orElse(null);
    }

    @Override
    public Booking updateBooking(Long id, Booking booking) {
        Booking existing = bookingRepository.findById(id).orElse(null);
        if (existing == null) return null;

        existing.setUser(booking.getUser());
        existing.setTrain(booking.getTrain());
        existing.setNumberOfTickets(booking.getNumberOfTickets());
        existing.setDepartureStation(booking.getDepartureStation());
        existing.setArrivalStation(booking.getArrivalStation());
        existing.setDepartureTime(booking.getDepartureTime());
        existing.setArrivalTime(booking.getArrivalTime());

        return bookingRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteBookingById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found: " + id));

        String email     = booking.getUser().getEmail();
        String username  = booking.getUser().getUsername();
        String trainName = booking.getTrain().getName();

        bookingRepository.delete(booking);

        notificationService.sendBookingCanceledNotification(email, username, trainName);
    }

    @Override
    public List<Booking> getBookingsByUser(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return bookingRepository.findByUserId(userId);
    }

    @Override
    public List<Booking> getBookingsByTrain(Long trainId) {
        trainRepository.findById(trainId)
                .orElseThrow(() -> new RuntimeException("Train not found"));

        return bookingRepository.findByTrainId(trainId);
    }
}
