package com.example.trainapplication.services;

import com.example.trainapplication.dtos.RouteDtos;
import com.example.trainapplication.model.Schedule;
import com.example.trainapplication.model.Station;
import com.example.trainapplication.model.Train;
import com.example.trainapplication.repositoires.IScheduleRepository;
import com.example.trainapplication.repositoires.IStationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.*;

@Service
public class RouteService {

    private final IStationRepository stationRepository;
    private final IScheduleRepository scheduleRepository;

    public RouteService(IStationRepository stationRepository, IScheduleRepository scheduleRepository) {
        this.stationRepository = stationRepository;
        this.scheduleRepository = scheduleRepository;
    }

    private record SearchNode(Station station, List<RouteDtos.RouteSegment> segmentsSoFar) {}

    public RouteDtos.RouteSearchResponse findRoute(String fromStationName, String toStationName) {
        Station origin = stationRepository.findByName(fromStationName);
        Station destination = stationRepository.findByName(toStationName);

        if (origin == null && destination == null) {
            throw new IllegalArgumentException(
                    "Stations not found: '" + fromStationName + "' and '" + toStationName + "'");
        }
        if (origin == null) {
            throw new IllegalArgumentException("Station not found: '" + fromStationName + "'");
        }
        if (destination == null) {
            throw new IllegalArgumentException("Station not found: '" + toStationName + "'");
        }

        Queue<SearchNode> queue = new LinkedList<>();
        Set<Long> visitedStationIds = new HashSet<>();

        queue.add(new SearchNode(origin, new ArrayList<>()));
        visitedStationIds.add(origin.getId());

        while (!queue.isEmpty()) {
            SearchNode current = queue.poll();
            Station currentStation = current.station();

            List<Schedule> schedulesAtCurrent = scheduleRepository
                    .findByStationId(currentStation.getId());

            for (Schedule departureSchedule : schedulesAtCurrent) {
                Train train = departureSchedule.getTrain();

                List<RouteDtos.RouteSegment> pastSegments = current.segmentsSoFar();
                if (!pastSegments.isEmpty()) {
                    RouteDtos.RouteSegment lastSegment = pastSegments.getLast();
                    LocalTime arrivalAtTransfer = lastSegment.arrival();
                    LocalTime departureOfNext = departureSchedule.getDepartureTime();
                    if (arrivalAtTransfer != null && departureOfNext != null) {
                        if (departureOfNext.isBefore(arrivalAtTransfer.plusMinutes(5))) {
                            continue;
                        }
                    }
                }

                List<Schedule> stopsOnTrain = scheduleRepository
                        .findByTrainIdOrderByStopOrderAsc(train.getId());

                for (Schedule nextStop : stopsOnTrain) {
                    if (nextStop.getStopOrder() <= departureSchedule.getStopOrder()) {
                        continue;
                    }

                    Station nextStation = nextStop.getStation();

                    RouteDtos.RouteSegment segment = new RouteDtos.RouteSegment(
                            train.getId(),
                            train.getName(),
                            currentStation.getName(),
                            nextStation.getName(),
                            departureSchedule.getDepartureTime(),
                            nextStop.getArrivalTime()
                    );

                    List<RouteDtos.RouteSegment> newSegments = new ArrayList<>(current.segmentsSoFar());
                    newSegments.add(segment);

                    if (nextStation.getId().equals(destination.getId())) {
                        return new RouteDtos.RouteSearchResponse(newSegments);
                    }

                    if (!visitedStationIds.contains(nextStation.getId())) {
                        visitedStationIds.add(nextStation.getId());
                        queue.add(new SearchNode(nextStation, newSegments));
                    }
                }
            }
        }
        throw new NoSuchElementException(
                "No route found between '" + fromStationName + "' and '" + toStationName + "'");
    }
}