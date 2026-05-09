package com.example.trainapplication.repositoires;

import com.example.trainapplication.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IScheduleRepository extends JpaRepository<Schedule, Long> {

    List<Schedule> findByTrainIdOrderByStopOrderAsc(Long trainId);
    List<Schedule> findByStationId(Long stationId);
    @Modifying
    @Query("DELETE FROM Schedule s WHERE s.train.id = :trainId")
    void deleteByTrainId(Long trainId);
}
