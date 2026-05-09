package com.example.trainapplication.services;

import com.example.trainapplication.contracts.StopRequest;
import com.example.trainapplication.contracts.TrainWithRouteRequest;
import com.example.trainapplication.dtos.TrainDtos;
import com.example.trainapplication.model.Train;
import jakarta.transaction.Transactional;

import java.util.List;

@Transactional
public interface ITrainService {

    List<TrainDtos.TrainResponse> getAll();
    Train addTrain(Train train);
    void deleteTrain(Long id);
    Train updateTrain(Long id, TrainWithRouteRequest train);
    Train updateDelay(Long trainId, Integer minutes);
    Train createTrainWithRoute(TrainWithRouteRequest request);
    List<StopRequest> getTrainRoute(Long id);
}
