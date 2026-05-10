package com.example.trainapplication.services;

import com.example.trainapplication.contracts.StopRequest;
import com.example.trainapplication.contracts.TrainWithRouteRequest;
import com.example.trainapplication.dtos.TrainDtos;
import com.example.trainapplication.model.Booking;
import com.example.trainapplication.model.Schedule;
import com.example.trainapplication.model.Station;
import com.example.trainapplication.model.Train;
import com.example.trainapplication.repositories.IBookingRepository;
import com.example.trainapplication.repositories.IScheduleRepository;
import com.example.trainapplication.repositories.IStationRepository;
import com.example.trainapplication.repositories.ITrainRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;

@Service
public class TrainService implements ITrainService {

    private final ITrainRepository trainRepository;
    private final IBookingRepository bookingRepository;
    private final INotificationService notificationService;
    private final IScheduleRepository scheduleRepository;
    private final IStationRepository stationRepository;

    public TrainService(ITrainRepository trainRepository,
                        IBookingRepository bookingRepository,
                        INotificationService notificationService,
                        IScheduleRepository scheduleRepository,
                        IStationRepository stationRepository) {
        this.trainRepository     = trainRepository;
        this.bookingRepository   = bookingRepository;
        this.notificationService = notificationService;
        this.scheduleRepository  = scheduleRepository;
        this.stationRepository   = stationRepository;
    }

    @Override
    public List<TrainDtos.TrainResponse> getAll() {
        return trainRepository.findAll().stream()
                .map(t -> {
                    Integer booked = bookingRepository.sumBookedTicketsByTrainId(t.getId());
                    return TrainDtos.TrainResponse.fromEntity(t, booked != null ? booked : 0);
                })
                .toList();
    }

    @Override
    public Train addTrain(Train train) {
        return trainRepository.save(train);
    }

    @Override
    @Transactional
    public void deleteTrain(Long id) {
        bookingRepository.deleteByTrainId(id);
        scheduleRepository.deleteByTrainId(id);
        trainRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Train updateTrain(Long id, TrainWithRouteRequest request) {
        Train existing = trainRepository.findById(id).orElse(null);
        if (existing == null) return null;

        existing.setName(request.name());
        existing.setTotalCapacity(request.totalCapacity());
        Train saved = trainRepository.save(existing);

        if (request.stops() != null && !request.stops().isEmpty()) {
            scheduleRepository.deleteByTrainId(saved.getId());
            saveStops(saved, request.stops());
        }
        return saved;
    }

    @Override
    @Transactional
    public Train updateDelay(Long trainId, Integer minutes) {
        Train train = trainRepository.findById(trainId)
                .orElseThrow(() -> new RuntimeException("Train not found: " + trainId));

        train.setDelayMinutes(minutes);
        Train updated = trainRepository.save(train);

        List<Booking> bookings = bookingRepository.findByTrainId(trainId);
        for (Booking b : bookings) {
            notificationService.sendDelayNotification(
                    b.getUser().getEmail(),
                    b.getUser().getUsername(),
                    train.getName(),
                    minutes
            );
        }
        return updated;
    }

    @Override
    @Transactional
    public Train createTrainWithRoute(TrainWithRouteRequest request) {
        Train train = new Train();
        train.setName(request.name());
        train.setTotalCapacity(request.totalCapacity());
        train.setDelayMinutes(0);
        Train saved = trainRepository.save(train);
        saveStops(saved, request.stops());
        return saved;
    }

    @Override
    public List<StopRequest> getTrainRoute(Long id) {
        return scheduleRepository.findByTrainIdOrderByStopOrderAsc(id).stream()
                .map(s -> new StopRequest(
                        s.getStation().getName(),
                        s.getArrivalTime()   != null ? s.getArrivalTime().toString()   : null,
                        s.getDepartureTime() != null ? s.getDepartureTime().toString() : null,
                        s.getStopOrder()
                )).toList();
    }

    private void saveStops(Train train, List<StopRequest> stops) {
        for (StopRequest stop : stops) {
            Station station = stationRepository.findByName(stop.stationName());
            if (station == null) {
                station = stationRepository.save(new Station(stop.stationName()));
            }
            Schedule schedule = new Schedule();
            schedule.setTrain(train);
            schedule.setStation(station);
            schedule.setArrivalTime(
                    stop.arrivalTime() != null && !stop.arrivalTime().isEmpty()
                            ? LocalTime.parse(stop.arrivalTime()) : null
            );
            schedule.setDepartureTime(
                    stop.departureTime() != null && !stop.departureTime().isEmpty()
                            ? LocalTime.parse(stop.departureTime()) : null
            );
            schedule.setStopOrder(stop.stopOrder());
            scheduleRepository.save(schedule);
        }
    }
}
