package com.example.trainapplication.controller;

import com.example.trainapplication.contracts.*;
import com.example.trainapplication.dtos.TrainDtos;
import com.example.trainapplication.model.Train;
import com.example.trainapplication.repositoires.IBookingRepository;
import com.example.trainapplication.services.ITrainService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/admin/trains")
public class TrainController {

    private final ITrainService trainService;
    private final IBookingRepository bookingRepository;

    public TrainController(ITrainService trainService,
                           IBookingRepository bookingRepository) {
        this.trainService = trainService;
        this.bookingRepository = bookingRepository;
    }

    @GetMapping
    public ResponseEntity<List<TrainDtos.TrainResponse>> getAll() {
        return ResponseEntity.ok(trainService.getAll());
    }

    @PostMapping
    public ResponseEntity<TrainDtos.TrainResponse> create(@RequestBody TrainRequest request) {
        Train t = new Train();
        t.setName(request.name());
        t.setTotalCapacity(request.totalCapacity());
        Train saved = trainService.addTrain(t);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(TrainDtos.TrainResponse.fromEntity(saved, 0));
    }

    @PutMapping("/delay")
    public ResponseEntity<TrainDtos.TrainResponse> setDelay(@RequestBody DelayRequest request) {
        Train updated = trainService.updateDelay(request.trainId(), request.delayMinutes());
        Integer booked = bookingRepository.sumBookedTicketsByTrainId(updated.getId());
        return ResponseEntity.ok().body(TrainDtos.TrainResponse.fromEntity(updated, booked != null ? booked : 0));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        trainService.deleteTrain(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/with-route")
    public ResponseEntity<TrainDtos.TrainResponse> createWithRoute(@RequestBody TrainWithRouteRequest request) {
        Train savedTrain = trainService.createTrainWithRoute(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(TrainDtos.TrainResponse.fromEntity(savedTrain, 0));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TrainDtos.TrainResponse> updateTrain(@PathVariable Long id, @RequestBody TrainWithRouteRequest request) {
        Train updatedTrain = trainService.updateTrain(id, request);
        if (updatedTrain == null) {
            return ResponseEntity.notFound().build();
        }
        Integer booked = bookingRepository.sumBookedTicketsByTrainId(updatedTrain.getId());
        return ResponseEntity.ok(TrainDtos.TrainResponse.fromEntity(updatedTrain, booked != null ? booked : 0));
    }

    @GetMapping("/{id}/route")
    public ResponseEntity<List<StopRequest>> getTrainRoute(@PathVariable Long id) {
        List<StopRequest> route = trainService.getTrainRoute(id);
        return ResponseEntity.ok(route);
    }
}