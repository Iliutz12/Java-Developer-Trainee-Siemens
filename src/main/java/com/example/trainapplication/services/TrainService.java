package com.example.trainapplication.services;

import com.example.trainapplication.contracts.StopRequest;
import com.example.trainapplication.contracts.TrainWithRouteRequest;
import com.example.trainapplication.dtos.TrainDtos;
import com.example.trainapplication.model.Booking;
import com.example.trainapplication.model.Schedule;
import com.example.trainapplication.model.Station;
import com.example.trainapplication.model.Train;
import com.example.trainapplication.repositoires.IBookingRepository;
import com.example.trainapplication.repositoires.IScheduleRepository;
import com.example.trainapplication.repositoires.IStationRepository;
import com.example.trainapplication.repositoires.ITrainRepository;
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
        this.trainRepository = trainRepository;
        this.bookingRepository = bookingRepository;
        this.notificationService = notificationService;
        this.scheduleRepository = scheduleRepository;
        this.stationRepository = stationRepository;
    }

    @Override
    public List<TrainDtos.TrainResponse> getAll() {
        return trainRepository.findAll().stream()
                .map(t -> {
                    Integer booked = bookingRepository.sumBookedTicketsByTrainId(t.getId());
                    int safeBookedCount = (booked != null) ? booked : 0;

                    return TrainDtos.TrainResponse.fromEntity(t, safeBookedCount);
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
        Train savedTrain = trainRepository.save(existing);

        if (request.stops() != null && !request.stops().isEmpty()) {
            scheduleRepository.deleteByTrainId(existing.getId());
            for (StopRequest stop : request.stops()) {
                Station station = stationRepository.findByName(stop.stationName());
                if (station == null) {
                    station = stationRepository.save(new Station(stop.stationName()));
                }
                Schedule schedule = new Schedule();
                schedule.setTrain(savedTrain);
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
        return savedTrain;
    }

    @Override
    @Transactional
    public Train updateDelay(Long trainId, Integer minutes) {
        Train train = trainRepository.findById(trainId)
                .orElseThrow(() -> new RuntimeException("Train not found: " + trainId));

        train.setDelayMinutes(minutes);
        Train updatedTrain = trainRepository.save(train);

        List<Booking> bookings = bookingRepository.findByTrainId(trainId);

        for (Booking b : bookings) {
            notificationService.sendDelayNotification(
                    b.getUser().getEmail(),
                    b.getUser().getUsername(),
                    train.getName(),
                    minutes
            );
        }
        return updatedTrain;
    }

    @Override
    @Transactional
    public Train createTrainWithRoute(TrainWithRouteRequest request) {
        Train train = new Train();
        train.setName(request.name());
        train.setTotalCapacity(request.totalCapacity());
        train.setDelayMinutes(0);
        Train savedTrain = trainRepository.save(train);

        for (StopRequest stop : request.stops()) {
            Station station = stationRepository.findByName(stop.stationName());
            if (station == null) {
                station = stationRepository.save(new Station(stop.stationName()));
            }
            Schedule schedule = new Schedule();
            schedule.setTrain(savedTrain);
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
        return savedTrain;
    }

    @Override
    public List<StopRequest> getTrainRoute(Long id) {
        return scheduleRepository.findByTrainIdOrderByStopOrderAsc(id).stream()
                .map(s -> new StopRequest(
                        s.getStation().getName(),
                        s.getArrivalTime() != null ? s.getArrivalTime().toString() : null,
                        s.getDepartureTime() != null ? s.getDepartureTime().toString() : null,
                        s.getStopOrder()
                )).toList();
    }
}