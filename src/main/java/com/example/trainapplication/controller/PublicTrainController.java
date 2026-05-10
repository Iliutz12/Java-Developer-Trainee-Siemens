package com.example.trainapplication.controller;

import com.example.trainapplication.dtos.TrainDtos;
import com.example.trainapplication.services.ITrainService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/trains")
public class PublicTrainController {

    private final ITrainService trainService;

    public PublicTrainController(ITrainService trainService) {
        this.trainService = trainService;
    }

    @GetMapping
    public ResponseEntity<List<TrainDtos.TrainResponse>> getAll() {
        return ResponseEntity.ok(trainService.getAll());
    }
}
