package com.example.trainapplication.controller;

import com.example.trainapplication.contracts.DelayRequest;
import com.example.trainapplication.contracts.StopRequest;
import com.example.trainapplication.contracts.TrainRequest;
import com.example.trainapplication.contracts.TrainWithRouteRequest;
import com.example.trainapplication.dtos.TrainDtos;
import com.example.trainapplication.model.Train;
import com.example.trainapplication.services.ITrainService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/admin/trains")
public class TrainController {

    private final ITrainService trainService;

    public TrainController(ITrainService trainService) {
        this.trainService = trainService;
    }

    @GetMapping
    public ResponseEntity<List<TrainDtos.TrainResponse>> getAll() {
        return ResponseEntity.ok(trainService.getAll());
    }

    @PostMapping
    public ResponseEntity<TrainDtos.TrainResponse> create(@Valid @RequestBody TrainRequest request) {
        Train t = new Train();
        t.setName(request.name());
        t.setTotalCapacity(request.totalCapacity());
        Train saved = trainService.addTrain(t);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(TrainDtos.TrainResponse.fromEntity(saved, 0));
    }

    @PostMapping("/with-route")
    public ResponseEntity<TrainDtos.TrainResponse> createWithRoute(
            @Valid @RequestBody TrainWithRouteRequest request) {
        Train saved = trainService.createTrainWithRoute(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(TrainDtos.TrainResponse.fromEntity(saved, 0));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTrain(
            @PathVariable Long id,
            @Valid @RequestBody TrainWithRouteRequest request) {
        Train updated = trainService.updateTrain(id, request);
        if (updated == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(TrainDtos.TrainResponse.fromEntity(updated, 0));
    }

    @PutMapping("/delay")
    public ResponseEntity<?> setDelay(@Valid @RequestBody DelayRequest request) {
        try {
            Train updated = trainService.updateDelay(request.trainId(), request.delayMinutes());
            return ResponseEntity.ok(TrainDtos.TrainResponse.fromEntity(updated, 0));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        trainService.deleteTrain(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/route")
    public ResponseEntity<List<StopRequest>> getTrainRoute(@PathVariable Long id) {
        return ResponseEntity.ok(trainService.getTrainRoute(id));
    }
}
